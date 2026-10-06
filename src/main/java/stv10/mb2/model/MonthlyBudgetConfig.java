package stv10.mb2.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "monthly_budget_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyBudgetConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String yearMonth;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private Integer vidaPercentage;

    @Column(nullable = false)
    private Integer ocioPercentage;

    @Column(nullable = false)
    private Integer inversionPercentage;
}
