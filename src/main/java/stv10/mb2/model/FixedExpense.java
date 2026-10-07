package stv10.mb2.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
public class FixedExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String description;
    
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;
    
    private Integer dueDay;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tag_id")
    private Tag tag;
}
