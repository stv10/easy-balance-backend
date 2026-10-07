package stv10.mb2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stv10.mb2.model.Account;
import stv10.mb2.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import stv10.mb2.dto.SSPRequest;
import stv10.mb2.dto.SSPResponse;
import stv10.mb2.repository.AccountRepository;
import stv10.mb2.repository.ExpenseRepository;
import stv10.mb2.strategy.ExpenseSpecificationBuilder;

import stv10.mb2.repository.TagRepository;
import stv10.mb2.dto.MonthlyExpensesAnalyticsDTO;
import stv10.mb2.dto.MonthlyTagSummaryDTO;
import stv10.mb2.dto.TagMonthHistoryDTO;
import stv10.mb2.model.Tag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AccountRepository accountRepository;
    private final TagRepository tagRepository;
    private final ExpenseSpecificationBuilder specificationBuilder;

    private static final UUID UNTAGGED_KEY = new UUID(0L, 0L);

    @Transactional
    public Expense addExpense(Expense expense) {
        expense.setCreatedAt(Optional.ofNullable(expense.getCreatedAt()).orElseGet(LocalDateTime::now));

        if (expense.getTag() != null && expense.getTag().getId() != null) {
            expense.setTag(tagRepository.findById(expense.getTag().getId()).orElse(null));
        } else {
            expense.setTag(null);
        }

        Expense savedExpense = expenseRepository.save(expense);

        Optional.ofNullable(expense.getAccountId())
                .ifPresent(accountId -> {
                    Account account = accountRepository.findById(accountId)
                            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
                    account.setBalance(account.getBalance().subtract(expense.getAmount()));
                    accountRepository.save(account);
                });

        return savedExpense;
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public SSPResponse<Expense> getExpensesSSP(SSPRequest request) {
        Specification<Expense> spec = specificationBuilder.build(request.getFilters());
        Pageable pageable = PageRequest.of(request.getPageIndex(), request.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Expense> page = expenseRepository.findAll(spec, pageable);

        return SSPResponse.<Expense>builder()
                .pageIndex(page.getNumber())
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .data(page.getContent())
                .build();
    }

    @Transactional
    public void deleteExpense(UUID id) {
        expenseRepository.findById(id).ifPresent(expense -> {
            Optional.ofNullable(expense.getAccountId())
                    .flatMap(accountRepository::findById)
                    .ifPresent(account -> {
                        account.setBalance(account.getBalance().add(expense.getAmount()));
                        accountRepository.save(account);
                    });
        });
        expenseRepository.deleteById(id);
    }

    public MonthlyExpensesAnalyticsDTO getMonthlyAnalytics(String yearMonthStr) {
        YearMonth ym = parseYearMonth(yearMonthStr);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59, 999999999);

        List<Expense> expenses = expenseRepository.findByCreatedAtBetween(start, end);
        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<UUID, List<Expense>> groupedByTagId = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.getTag() != null ? e.getTag().getId() : UNTAGGED_KEY));

        List<MonthlyTagSummaryDTO> summaries = new ArrayList<>();

        for (Map.Entry<UUID, List<Expense>> entry : groupedByTagId.entrySet()) {
            List<Expense> groupExpenses = entry.getValue();
            BigDecimal groupTotal = groupExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Double percentage = 0.0;
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                percentage = groupTotal.multiply(BigDecimal.valueOf(100))
                        .divide(totalAmount, 2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            Expense firstWithTag = groupExpenses.stream()
                    .filter(e -> e.getTag() != null)
                    .findFirst()
                    .orElse(null);

            if (firstWithTag != null && firstWithTag.getTag() != null) {
                Tag tag = firstWithTag.getTag();
                summaries.add(MonthlyTagSummaryDTO.builder()
                        .tagId(tag.getId())
                        .tagName(tag.getName())
                        .tagIcon(tag.getIcon())
                        .tagColor(tag.getColor())
                        .totalAmount(groupTotal)
                        .percentage(percentage)
                        .count(groupExpenses.size())
                        .build());
            } else {
                summaries.add(MonthlyTagSummaryDTO.builder()
                        .tagId(null)
                        .tagName("Sin etiqueta")
                        .tagIcon("HelpCircle")
                        .tagColor("#94a3b8")
                        .totalAmount(groupTotal)
                        .percentage(percentage)
                        .count(groupExpenses.size())
                        .build());
            }
        }

        summaries.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));

        return MonthlyExpensesAnalyticsDTO.builder()
                .yearMonth(ym.toString())
                .totalAmount(totalAmount)
                .tagSummaries(summaries)
                .build();
    }

    public List<TagMonthHistoryDTO> getTagHistory(UUID tagId, String yearMonthStr) {
        YearMonth centerYm = parseYearMonth(yearMonthStr);
        List<TagMonthHistoryDTO> history = new ArrayList<>();

        for (int i = -3; i <= 2; i++) {
            YearMonth targetYm = centerYm.plusMonths(i);
            LocalDateTime start = targetYm.atDay(1).atStartOfDay();
            LocalDateTime end = targetYm.atEndOfMonth().atTime(23, 59, 59, 999999999);

            List<Expense> monthExpenses;
            if (tagId != null) {
                monthExpenses = expenseRepository.findByTagIdAndCreatedAtBetween(tagId, start, end);
            } else {
                monthExpenses = expenseRepository.findByTagIsNullAndCreatedAtBetween(start, end);
            }

            BigDecimal monthTotal = monthExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            history.add(TagMonthHistoryDTO.builder()
                    .yearMonth(targetYm.toString())
                    .label(formatMonthLabel(targetYm))
                    .amount(monthTotal)
                    .isCurrent(i == 0)
                    .build());
        }

        return history;
    }

    public List<Expense> getExpensesByTag(UUID tagId, String yearMonthStr) {
        YearMonth ym = parseYearMonth(yearMonthStr);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59, 999999999);

        if (tagId != null) {
            return expenseRepository.findByTagIdAndCreatedAtBetween(tagId, start, end);
        } else {
            return expenseRepository.findByTagIsNullAndCreatedAtBetween(start, end);
        }
    }

    @Transactional
    public Expense updateExpenseTag(UUID expenseId, UUID tagId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found: " + expenseId));

        if (tagId != null) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));
            expense.setTag(tag);
        } else {
            expense.setTag(null);
        }

        return expenseRepository.save(expense);
    }

    @Transactional
    public List<Expense> bulkUpdateExpenseTag(List<UUID> expenseIds, UUID tagId) {
        Tag tag = null;
        if (tagId != null) {
            tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));
        }

        List<Expense> expenses = expenseRepository.findAllById(expenseIds);
        for (Expense expense : expenses) {
            expense.setTag(tag);
        }

        return expenseRepository.saveAll(expenses);
    }

    private YearMonth parseYearMonth(String yearMonthStr) {
        if (yearMonthStr != null && !yearMonthStr.trim().isEmpty()) {
            try {
                return YearMonth.parse(yearMonthStr.trim());
            } catch (Exception ignored) {
            }
        }
        return YearMonth.now();
    }

    private String formatMonthLabel(YearMonth ym) {
        String formatted = ym.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.forLanguageTag("es")));
        return formatted.substring(0, 1).toUpperCase(Locale.ROOT) + formatted.substring(1).replace(".", "");
    }
}
