package stv10.mb2.dto;

import java.math.BigDecimal;

public record CreateAccountDTO(
    String name,
    BigDecimal balance
) {}
