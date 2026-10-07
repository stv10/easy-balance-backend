package stv10.mb2.dto;

import stv10.mb2.model.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateExpenseDTO(
    UUID id,
    String description,
    BigDecimal amount,
    ExpenseCategory category,
    LocalDateTime createdAt,
    UUID accountId,
    UUID tagId,
    UUID fixedExpenseId
) {}
