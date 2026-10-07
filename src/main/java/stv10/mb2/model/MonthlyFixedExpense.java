package stv10.mb2.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyFixedExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String yearMonth; // e.g. "2026-08"
    
    private String description;
    
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;
    
    private Integer dueDay;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tag_id")
    private Tag tag;
}
