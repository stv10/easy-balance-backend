package stv10.mb2.dto;

import lombok.Builder;
import lombok.Data;
import stv10.mb2.model.ExpenseCategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardSummaryDTO {
    private BigDecimal totalBudget;
    private BigDecimal totalRemaining;
    private Map<ExpenseCategory, CategorySummaryDTO> categories;
    private List<MonthlyFixedExpenseDTO> fixedExpenses;
    private boolean generated;
    private MonthlyBudgetConfigDTO budgetConfig;
}
