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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AccountRepository accountRepository;
    private final ExpenseSpecificationBuilder specificationBuilder;

    @Transactional
    public Expense addExpense(Expense expense) {
        expense.setCreatedAt(Optional.ofNullable(expense.getCreatedAt()).orElseGet(LocalDateTime::now));
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
}
