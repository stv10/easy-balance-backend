package stv10.mb2.dto;

import java.math.BigDecimal;

public record UpdateAccountDTO(
    String name,
    BigDecimal balance
) {}
