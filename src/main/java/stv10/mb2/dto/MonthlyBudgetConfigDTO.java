package stv10.mb2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyBudgetConfigDTO {
    private BigDecimal totalAmount;
    private Integer vidaPercentage;
    private Integer ocioPercentage;
    private Integer inversionPercentage;
    private Boolean isCustom;
}
