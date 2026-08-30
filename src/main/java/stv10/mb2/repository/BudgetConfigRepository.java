package stv10.mb2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stv10.mb2.model.BudgetConfig;
import java.util.UUID;

public interface BudgetConfigRepository extends JpaRepository<BudgetConfig, UUID> {
}
