package stv10.mb2.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CategorySummaryDTO {
    private BigDecimal assignedBudget;
    private BigDecimal totalFixedExpenses;
    private BigDecimal totalVariableExpenses;
    private BigDecimal remainingBudget;
}
