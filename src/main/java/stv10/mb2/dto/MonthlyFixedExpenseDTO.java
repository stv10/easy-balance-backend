package stv10.mb2.dto;

import lombok.Builder;
import lombok.Data;
import stv10.mb2.model.ExpenseCategory;

import stv10.mb2.model.Tag;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class MonthlyFixedExpenseDTO {
    private UUID id;
    private String description;
    private BigDecimal amount;
    private ExpenseCategory category;
    private Integer dueDay;
    private boolean paid;
    private BigDecimal actualAmount;
    private UUID expenseId;
    private UUID accountId;
    private Tag tag;
}
