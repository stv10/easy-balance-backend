package stv10.mb2.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String description;
    
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private UUID accountId;
    
    private UUID fixedExpenseId;
}
