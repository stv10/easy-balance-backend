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
public class TagMonthHistoryDTO {
    private String yearMonth;
    private String label;
    private BigDecimal amount;
    private boolean isCurrent;
}
