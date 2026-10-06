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
import stv10.mb2.dto.MonthlyBudgetConfigDTO;
import stv10.mb2.dto.MonthlyFixedExpenseDTO;
import stv10.mb2.model.BudgetConfig;
import stv10.mb2.model.Expense;
import stv10.mb2.model.ExpenseCategory;
import stv10.mb2.model.FixedExpense;
import stv10.mb2.model.MonthlyBudgetConfig;
import stv10.mb2.model.MonthlyFixedExpense;
import stv10.mb2.repository.BudgetConfigRepository;
import stv10.mb2.repository.ExpenseRepository;
import stv10.mb2.repository.FixedExpenseRepository;
import stv10.mb2.repository.MonthlyBudgetConfigRepository;
import stv10.mb2.repository.MonthlyFixedExpenseRepository;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final BudgetConfigRepository budgetConfigRepository;
  private final MonthlyBudgetConfigRepository monthlyBudgetConfigRepository;
  private final FixedExpenseRepository fixedExpenseRepository;
  private final ExpenseRepository expenseRepository;
  private final MonthlyFixedExpenseRepository monthlyFixedExpenseRepository;
  private final ExpenseService expenseService;

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

    // Resolve budget config: monthly specific if present, else fallback to global
    Optional<MonthlyBudgetConfig> monthlyConfigOpt = monthlyBudgetConfigRepository.findByYearMonth(finalYearMonthStr);
    Optional<BudgetConfig> globalConfigOpt = budgetConfigRepository.findAll().stream().findFirst();

    BigDecimal totalBudget;
    Integer vidaPct;
    Integer ocioPct;
    Integer inversionPct;
    boolean isCustom;

    if (monthlyConfigOpt.isPresent()) {
      MonthlyBudgetConfig mc = monthlyConfigOpt.get();
      totalBudget = mc.getTotalAmount();
      vidaPct = mc.getVidaPercentage();
      ocioPct = mc.getOcioPercentage();
      inversionPct = mc.getInversionPercentage();
      isCustom = true;
    } else if (globalConfigOpt.isPresent() && globalConfigOpt.get().getTotalAmount() != null) {
      BudgetConfig gc = globalConfigOpt.get();
      totalBudget = gc.getTotalAmount();
      vidaPct = gc.getVidaPercentage();
      ocioPct = gc.getOcioPercentage();
      inversionPct = gc.getInversionPercentage();
      isCustom = false;
    } else {
      return DashboardSummaryDTO.builder()
          .totalBudget(BigDecimal.ZERO)
          .totalRemaining(BigDecimal.ZERO)
          .categories(Map.of())
          .fixedExpenses(List.of())
          .generated(false)
          .budgetConfig(null)
          .build();
    }

    MonthlyBudgetConfigDTO budgetConfigDTO = MonthlyBudgetConfigDTO.builder()
        .totalAmount(totalBudget)
        .vidaPercentage(Optional.ofNullable(vidaPct).orElse(0))
        .ocioPercentage(Optional.ofNullable(ocioPct).orElse(0))
        .inversionPercentage(Optional.ofNullable(inversionPct).orElse(0))
        .isCustom(isCustom)
        .build();

    boolean isGenerated = monthlyFixedExpenseRepository.existsByYearMonth(finalYearMonthStr);

    List<MonthlyFixedExpense> fixedExpenses = monthlyFixedExpenseRepository
        .findByYearMonth(finalYearMonthStr).stream()
        .sorted(Comparator.comparing(MonthlyFixedExpense::getDueDay,
            Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();

    LocalDateTime start = finalYearMonth.atDay(1).atStartOfDay();
    LocalDateTime end = finalYearMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);
    List<Expense> expenses = expenseRepository.findByCreatedAtBetween(start, end);

    List<MonthlyFixedExpenseDTO> monthlyFixedExpenses = new ArrayList<>();
    for (MonthlyFixedExpense fe : fixedExpenses) {
      Optional<Expense> paymentExpenseOpt = expenses.stream()
          .filter(e -> e.getFixedExpenseId() != null && e.getFixedExpenseId().equals(fe.getId()))
          .findFirst();

      monthlyFixedExpenses.add(MonthlyFixedExpenseDTO.builder()
          .id(fe.getId())
          .description(fe.getDescription())
          .amount(fe.getAmount())
          .category(fe.getCategory())
          .dueDay(fe.getDueDay())
          .paid(paymentExpenseOpt.isPresent())
          .actualAmount(paymentExpenseOpt.map(Expense::getAmount).orElse(null))
          .expenseId(paymentExpenseOpt.map(Expense::getId).orElse(null))
          .accountId(paymentExpenseOpt.map(Expense::getAccountId).orElse(null))
          .build());
    }

    Map<ExpenseCategory, CategorySummaryDTO> categoryMap = new HashMap<>();
    BigDecimal totalRemaining = BigDecimal.ZERO;

    for (ExpenseCategory category : ExpenseCategory.values()) {
      Integer percentage = getPercentageForCategory(vidaPct, ocioPct, inversionPct, category);
      BigDecimal assignedBudget = totalBudget
          .multiply(new BigDecimal(percentage))
          .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);

      BigDecimal totalFixed = fixedExpenses.stream()
          .filter(fe -> fe.getCategory() == category)
          .map(MonthlyFixedExpense::getAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal actualPaidFixed = monthlyFixedExpenses.stream()
          .filter(mfe -> mfe.getCategory() == category && mfe.isPaid())
          .map(MonthlyFixedExpenseDTO::getActualAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal unpaidBudgetedFixed = monthlyFixedExpenses.stream()
          .filter(mfe -> mfe.getCategory() == category && !mfe.isPaid())
          .map(MonthlyFixedExpenseDTO::getAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal totalVariable = expenses.stream()
          .filter(e -> e.getCategory() == category && e.getFixedExpenseId() == null)
          .map(Expense::getAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

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
        .totalBudget(totalBudget)
        .totalRemaining(totalRemaining)
        .categories(categoryMap)
        .fixedExpenses(monthlyFixedExpenses)
        .generated(isGenerated)
        .budgetConfig(budgetConfigDTO)
        .build();
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

    // Snapshot budget config from global config if not already customized for this month
    if (!monthlyBudgetConfigRepository.existsByYearMonth(yearMonthStr)) {
      budgetConfigRepository.findAll().stream().findFirst()
          .filter(config -> config.getTotalAmount() != null)
          .ifPresent(config -> {
            MonthlyBudgetConfig snapshot = MonthlyBudgetConfig.builder()
                .yearMonth(yearMonthStr)
                .totalAmount(config.getTotalAmount())
                .vidaPercentage(Optional.ofNullable(config.getVidaPercentage()).orElse(0))
                .ocioPercentage(Optional.ofNullable(config.getOcioPercentage()).orElse(0))
                .inversionPercentage(Optional.ofNullable(config.getInversionPercentage()).orElse(0))
                .build();
            monthlyBudgetConfigRepository.save(snapshot);
          });
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

  @Transactional
  public DashboardSummaryDTO regenerateMonthlyExpenses(String yearMonthStr) {
    try {
      YearMonth.parse(yearMonthStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid yearMonth format: " + yearMonthStr);
    }

    List<MonthlyFixedExpense> existingFixedExpenses = monthlyFixedExpenseRepository.findByYearMonth(yearMonthStr);

    // Delete any Expense linked to these monthly fixed expenses (which restores account balance)
    for (MonthlyFixedExpense fe : existingFixedExpenses) {
      List<Expense> linkedExpenses = expenseRepository.findByFixedExpenseId(fe.getId());
      for (Expense exp : linkedExpenses) {
        expenseService.deleteExpense(exp.getId());
      }
    }

    // Delete existing monthly fixed expenses
    monthlyFixedExpenseRepository.deleteAll(existingFixedExpenses);

    // Re-create from templates (preserve month's custom budget)
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

  @Transactional
  public DashboardSummaryDTO updateMonthlyBudget(String yearMonthStr, MonthlyBudgetConfigDTO dto) {
    try {
      YearMonth.parse(yearMonthStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid yearMonth format: " + yearMonthStr);
    }

    if (dto == null || dto.getTotalAmount() == null || dto.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Total amount must be greater than or equal to 0");
    }

    int vida = Optional.ofNullable(dto.getVidaPercentage()).orElse(0);
    int ocio = Optional.ofNullable(dto.getOcioPercentage()).orElse(0);
    int inversion = Optional.ofNullable(dto.getInversionPercentage()).orElse(0);

    if (vida + ocio + inversion != 100) {
      throw new IllegalArgumentException("Sum of percentages must equal 100%");
    }

    MonthlyBudgetConfig mbc = monthlyBudgetConfigRepository.findByYearMonth(yearMonthStr)
        .orElseGet(() -> MonthlyBudgetConfig.builder().yearMonth(yearMonthStr).build());

    mbc.setTotalAmount(dto.getTotalAmount());
    mbc.setVidaPercentage(vida);
    mbc.setOcioPercentage(ocio);
    mbc.setInversionPercentage(inversion);

    monthlyBudgetConfigRepository.save(mbc);
    return getSummary(yearMonthStr);
  }

  @Transactional
  public DashboardSummaryDTO resetMonthlyBudget(String yearMonthStr) {
    try {
      YearMonth.parse(yearMonthStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid yearMonth format: " + yearMonthStr);
    }

    monthlyBudgetConfigRepository.findByYearMonth(yearMonthStr)
        .ifPresent(monthlyBudgetConfigRepository::delete);

    // If the month is already generated, re-snapshot from the current global template
    if (monthlyFixedExpenseRepository.existsByYearMonth(yearMonthStr)) {
      budgetConfigRepository.findAll().stream().findFirst()
          .filter(config -> config.getTotalAmount() != null)
          .ifPresent(config -> {
            MonthlyBudgetConfig snapshot = MonthlyBudgetConfig.builder()
                .yearMonth(yearMonthStr)
                .totalAmount(config.getTotalAmount())
                .vidaPercentage(Optional.ofNullable(config.getVidaPercentage()).orElse(0))
                .ocioPercentage(Optional.ofNullable(config.getOcioPercentage()).orElse(0))
                .inversionPercentage(Optional.ofNullable(config.getInversionPercentage()).orElse(0))
                .build();
            monthlyBudgetConfigRepository.save(snapshot);
          });
    }

    return getSummary(yearMonthStr);
  }

  private Integer getPercentageForCategory(Integer vida, Integer ocio, Integer inversion, ExpenseCategory category) {
    return switch (category) {
      case VIDA -> Optional.ofNullable(vida).orElse(0);
      case OCIO -> Optional.ofNullable(ocio).orElse(0);
      case INVERSION -> Optional.ofNullable(inversion).orElse(0);
    };
  }
}
