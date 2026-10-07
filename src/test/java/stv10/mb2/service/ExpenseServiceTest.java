package stv10.mb2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stv10.mb2.dto.MonthlyExpensesAnalyticsDTO;
import stv10.mb2.dto.TagMonthHistoryDTO;
import stv10.mb2.model.Expense;
import stv10.mb2.model.ExpenseCategory;
import stv10.mb2.model.Tag;
import stv10.mb2.repository.AccountRepository;
import stv10.mb2.repository.ExpenseRepository;
import stv10.mb2.repository.TagRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private Tag foodTag;
    private UUID foodTagId;

    @BeforeEach
    void setUp() {
        foodTagId = UUID.randomUUID();
        foodTag = Tag.builder()
                .id(foodTagId)
                .name("Comida")
                .icon("Utensils")
                .color("#10B981")
                .build();
    }

    @Test
    void getMonthlyAnalytics_CalculatesTotalsAndPercentages() {
        Expense e1 = new Expense();
        e1.setId(UUID.randomUUID());
        e1.setAmount(new BigDecimal("300.00"));
        e1.setCategory(ExpenseCategory.VIDA);
        e1.setTag(foodTag);
        e1.setCreatedAt(LocalDateTime.of(2026, 10, 5, 12, 0));

        Expense e2 = new Expense();
        e2.setId(UUID.randomUUID());
        e2.setAmount(new BigDecimal("100.00"));
        e2.setCategory(ExpenseCategory.OCIO);
        e2.setTag(null); // Untagged
        e2.setCreatedAt(LocalDateTime.of(2026, 10, 10, 14, 0));

        when(expenseRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(e1, e2));

        MonthlyExpensesAnalyticsDTO analytics = expenseService.getMonthlyAnalytics("2026-10");

        assertNotNull(analytics);
        assertEquals(new BigDecimal("400.00"), analytics.getTotalAmount());
        assertEquals(2, analytics.getTagSummaries().size());

        // First should be food tag with 300 (75%)
        assertEquals(foodTagId, analytics.getTagSummaries().get(0).getTagId());
        assertEquals("Comida", analytics.getTagSummaries().get(0).getTagName());
        assertEquals(new BigDecimal("300.00"), analytics.getTagSummaries().get(0).getTotalAmount());
        assertEquals(75.0, analytics.getTagSummaries().get(0).getPercentage());

        // Second should be untagged with 100 (25%)
        assertNull(analytics.getTagSummaries().get(1).getTagId());
        assertEquals("Sin etiqueta", analytics.getTagSummaries().get(1).getTagName());
        assertEquals(new BigDecimal("100.00"), analytics.getTagSummaries().get(1).getTotalAmount());
        assertEquals(25.0, analytics.getTagSummaries().get(1).getPercentage());
    }

    @Test
    void getTagHistory_Returns6MonthsWithCurrentInMiddle() {
        when(expenseRepository.findByTagIdAndCreatedAtBetween(eq(foodTagId), any(), any()))
                .thenReturn(List.of());

        List<TagMonthHistoryDTO> history = expenseService.getTagHistory(foodTagId, "2026-10");

        assertNotNull(history);
        assertEquals(6, history.size());
        assertEquals("2026-07", history.get(0).getYearMonth());
        assertFalse(history.get(0).isCurrent());

        // Month 3 (4th item, index 3) is 2026-10 (current)
        assertEquals("2026-10", history.get(3).getYearMonth());
        assertTrue(history.get(3).isCurrent());

        assertEquals("2026-12", history.get(5).getYearMonth());
        assertFalse(history.get(5).isCurrent());
    }

    @Test
    void bulkUpdateExpenseTag_UpdatesTagsCorrectly() {
        UUID e1Id = UUID.randomUUID();
        UUID e2Id = UUID.randomUUID();

        Expense e1 = new Expense();
        e1.setId(e1Id);
        Expense e2 = new Expense();
        e2.setId(e2Id);

        when(tagRepository.findById(foodTagId)).thenReturn(Optional.of(foodTag));
        when(expenseRepository.findAllById(List.of(e1Id, e2Id))).thenReturn(List.of(e1, e2));
        when(expenseRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Expense> updated = expenseService.bulkUpdateExpenseTag(List.of(e1Id, e2Id), foodTagId);

        assertEquals(2, updated.size());
        assertEquals(foodTag, e1.getTag());
        assertEquals(foodTag, e2.getTag());
        verify(expenseRepository).saveAll(any());
    }

    @Test
    void createExpenses_SavesExpensesAndDeductsAccountBalance() {
        UUID accountId = UUID.randomUUID();
        stv10.mb2.model.Account account = new stv10.mb2.model.Account();
        account.setId(accountId);
        account.setBalance(new BigDecimal("1000.00"));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(tagRepository.findById(foodTagId)).thenReturn(Optional.of(foodTag));
        when(expenseRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        stv10.mb2.dto.CreateExpenseDTO dto = new stv10.mb2.dto.CreateExpenseDTO(
                "Supermercado",
                new BigDecimal("250.00"),
                ExpenseCategory.VIDA,
                LocalDateTime.now(),
                accountId,
                foodTagId,
                null
        );

        List<Expense> saved = expenseService.createExpenses(List.of(dto));

        assertEquals(1, saved.size());
        assertEquals("Supermercado", saved.get(0).getDescription());
        assertEquals(new BigDecimal("750.00"), account.getBalance());
        verify(accountRepository).save(account);
        verify(expenseRepository).saveAll(any());
    }

    @Test
    void updateExpenses_RebalancesAccountBalanceCorrectly() {
        UUID oldAccountId = UUID.randomUUID();
        stv10.mb2.model.Account oldAccount = new stv10.mb2.model.Account();
        oldAccount.setId(oldAccountId);
        oldAccount.setBalance(new BigDecimal("500.00"));

        UUID newAccountId = UUID.randomUUID();
        stv10.mb2.model.Account newAccount = new stv10.mb2.model.Account();
        newAccount.setId(newAccountId);
        newAccount.setBalance(new BigDecimal("1000.00"));

        UUID expenseId = UUID.randomUUID();
        Expense existingExpense = new Expense();
        existingExpense.setId(expenseId);
        existingExpense.setDescription("Old Description");
        existingExpense.setAmount(new BigDecimal("100.00"));
        existingExpense.setAccountId(oldAccountId);

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(existingExpense));
        when(accountRepository.findById(oldAccountId)).thenReturn(Optional.of(oldAccount));
        when(accountRepository.findById(newAccountId)).thenReturn(Optional.of(newAccount));
        when(expenseRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        stv10.mb2.dto.UpdateExpenseDTO updateDto = new stv10.mb2.dto.UpdateExpenseDTO(
                expenseId,
                "New Description",
                new BigDecimal("150.00"),
                ExpenseCategory.OCIO,
                LocalDateTime.now(),
                newAccountId,
                null,
                null
        );

        List<Expense> updated = expenseService.updateExpenses(List.of(updateDto));

        assertEquals(1, updated.size());
        assertEquals("New Description", updated.get(0).getDescription());
        assertEquals(new BigDecimal("600.00"), oldAccount.getBalance()); // Reverted old 100
        assertEquals(new BigDecimal("850.00"), newAccount.getBalance()); // Deducted new 150
        verify(accountRepository).save(oldAccount);
        verify(accountRepository).save(newAccount);
    }
}
