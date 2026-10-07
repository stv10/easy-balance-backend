package stv10.mb2.dto;

import java.math.BigDecimal;

public record UpdateBudgetConfigDTO(
    BigDecimal totalAmount,
    Integer vidaPercentage,
    Integer ocioPercentage,
    Integer inversionPercentage
) {}
