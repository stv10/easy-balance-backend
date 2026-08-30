package stv10.mb2.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import stv10.mb2.dto.CategorySummaryDTO;
import stv10.mb2.dto.DashboardSummaryDTO;
import stv10.mb2.dto.MonthlyFixedExpenseDTO;
import stv10.mb2.model.BudgetConfig;
import stv10.mb2.model.Expense;
import stv10.mb2.model.ExpenseCategory;
import stv10.mb2.model.FixedExpense;
import stv10.mb2.model.MonthlyFixedExpense;
import stv10.mb2.repository.BudgetConfigRepository;
import stv10.mb2.repository.ExpenseRepository;
import stv10.mb2.repository.FixedExpenseRepository;
import stv10.mb2.repository.MonthlyFixedExpenseRepository;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final BudgetConfigRepository budgetConfigRepository;
  private final FixedExpenseRepository fixedExpenseRepository;
  private final ExpenseRepository expenseRepository;
  private final MonthlyFixedExpenseRepository monthlyFixedExpenseRepository;

  public DashboardSummaryDTO getSummary(String yearMonthStr) {
    YearMonth yearMonth;
    try {
      yearMonth = (yearMonthStr != null && !yearMonthStr.trim().isEmpty())
          ? YearMonth.parse(yearMonthStr)
          : YearMonth.now();
    } catch (Exception e) {
      yearMonth = YearMonth.now();
    }
    final String finalYearMonthStr = yearMonth.toString();
    final YearMonth finalYearMonth = yearMonth;

    return budgetConfigRepository.findAll().stream().findFirst()
        .filter(config -> config.getTotalAmount() != null)
        .map(config -> {
          boolean isGenerated = monthlyFixedExpenseRepository
              .existsByYearMonth(finalYearMonthStr);

          List<MonthlyFixedExpense> fixedExpenses = monthlyFixedExpenseRepository
              .findByYearMonth(finalYearMonthStr).stream()
              .sorted(Comparator.comparing(MonthlyFixedExpense::getDueDay,
                  Comparator.nullsLast(
                      Comparator.naturalOrder())))
              .toList();

          LocalDateTime start = finalYearMonth.atDay(1).atStartOfDay();
          LocalDateTime end = finalYearMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);
          List<Expense> expenses = expenseRepository.findByCreatedAtBetween(start, end);

          List<MonthlyFixedExpenseDTO> monthlyFixedExpenses = new ArrayList<>();
          for (MonthlyFixedExpense fe : fixedExpenses) {
            Optional<Expense> paymentExpenseOpt = expenses.stream()
                .filter(e -> e.getFixedExpenseId() != null && e
                    .getFixedExpenseId().equals(fe.getId()))
                .findFirst();

            monthlyFixedExpenses.add(MonthlyFixedExpenseDTO.builder()
                .id(fe.getId())
                .description(fe.getDescription())
                .amount(fe.getAmount())
                .category(fe.getCategory())
                .dueDay(fe.getDueDay())
                .paid(paymentExpenseOpt.isPresent())
                .actualAmount(paymentExpenseOpt.map(Expense::getAmount)
                    .orElse(null))
                .expenseId(paymentExpenseOpt.map(Expense::getId)
                    .orElse(null))
                .accountId(paymentExpenseOpt.map(Expense::getAccountId)
                    .orElse(null))
                .build());
          }

          Map<ExpenseCategory, CategorySummaryDTO> categoryMap = new HashMap<>();
          BigDecimal totalRemaining = BigDecimal.ZERO;

          for (ExpenseCategory category : ExpenseCategory.values()) {
            Integer percentage = getPercentageForCategory(config, category);
            BigDecimal assignedBudget = config.getTotalAmount()
                .multiply(new BigDecimal(percentage))
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);

            BigDecimal totalFixed = fixedExpenses.stream()
                .filter(fe -> fe.getCategory() == category)
                .map(fe -> fe.getAmount())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

            BigDecimal actualPaidFixed = monthlyFixedExpenses.stream()
                .filter(mfe -> mfe.getCategory() == category
                    && mfe.isPaid())
                .map(mfe -> mfe.getActualAmount())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

            BigDecimal unpaidBudgetedFixed = monthlyFixedExpenses.stream()
                .filter(mfe -> mfe.getCategory() == category
                    && !mfe.isPaid())
                .map(mfe -> mfe.getAmount())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

            BigDecimal totalVariable = expenses.stream()
                .filter(e -> e.getCategory() == category
                    && e.getFixedExpenseId() == null)
                .map(e -> e.getAmount())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

            BigDecimal remaining = assignedBudget.subtract(actualPaidFixed)
                .subtract(unpaidBudgetedFixed).subtract(totalVariable);
            totalRemaining = totalRemaining.add(remaining);

            categoryMap.put(category, CategorySummaryDTO.builder()
                .assignedBudget(assignedBudget)
                .totalFixedExpenses(totalFixed)
                .totalVariableExpenses(totalVariable)
                .remainingBudget(remaining)
                .build());
          }

          return DashboardSummaryDTO.builder()
              .totalBudget(config.getTotalAmount())
              .totalRemaining(totalRemaining)
              .categories(categoryMap)
              .fixedExpenses(monthlyFixedExpenses)
              .generated(isGenerated)
              .build();
        })
        .orElseGet(() -> DashboardSummaryDTO.builder()
            .totalBudget(BigDecimal.ZERO)
            .totalRemaining(BigDecimal.ZERO)
            .categories(Map.of())
            .fixedExpenses(List.of())
            .generated(false)
            .build());
  }

  @Transactional
  public DashboardSummaryDTO generateMonthlyExpenses(String yearMonthStr) {
    try {
      YearMonth.parse(yearMonthStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid yearMonth format: " + yearMonthStr);
    }

    if (monthlyFixedExpenseRepository.existsByYearMonth(yearMonthStr)) {
      throw new IllegalStateException("Fixed expenses already generated for month " + yearMonthStr);
    }

    List<FixedExpense> templates = fixedExpenseRepository.findAll();
    List<MonthlyFixedExpense> instances = templates.stream()
        .map(t -> MonthlyFixedExpense.builder()
            .yearMonth(yearMonthStr)
            .description(t.getDescription())
            .amount(t.getAmount())
            .category(t.getCategory())
            .dueDay(t.getDueDay())
            .build())
        .toList();

    monthlyFixedExpenseRepository.saveAll(instances);
    return getSummary(yearMonthStr);
  }

  private Integer getPercentageForCategory(BudgetConfig config, ExpenseCategory category) {
    return switch (category) {
      case VIDA -> Optional.ofNullable(config.getVidaPercentage()).orElse(0);
      case OCIO -> Optional.ofNullable(config.getOcioPercentage()).orElse(0);
      case INVERSION -> Optional.ofNullable(config.getInversionPercentage()).orElse(0);
    };
  }
}
