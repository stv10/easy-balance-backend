package stv10.mb2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTagSummaryDTO {
    private UUID tagId;
    private String tagName;
    private String tagIcon;
    private String tagColor;
    private BigDecimal totalAmount;
    private Double percentage;
    private Integer count;
}
