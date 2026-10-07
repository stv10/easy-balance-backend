package stv10.mb2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyExpensesAnalyticsDTO {
    private String yearMonth;
    private BigDecimal totalAmount;
    private List<MonthlyTagSummaryDTO> tagSummaries;
}
