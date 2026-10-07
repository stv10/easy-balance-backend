package stv10.mb2.dto;

import stv10.mb2.model.ExpenseCategory;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateFixedExpenseDTO(
    String description,
    BigDecimal amount,
    ExpenseCategory category,
    Integer dueDay,
    UUID tagId
) {}
