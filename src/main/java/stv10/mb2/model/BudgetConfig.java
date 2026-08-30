package stv10.mb2.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
public class BudgetConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private BigDecimal totalAmount;
    
    private Integer vidaPercentage;
    
    private Integer ocioPercentage;
    
    private Integer inversionPercentage;
}
