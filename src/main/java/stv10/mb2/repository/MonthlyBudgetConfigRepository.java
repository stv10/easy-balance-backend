package stv10.mb2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stv10.mb2.model.MonthlyBudgetConfig;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonthlyBudgetConfigRepository extends JpaRepository<MonthlyBudgetConfig, UUID> {
    Optional<MonthlyBudgetConfig> findByYearMonth(String yearMonth);
    boolean existsByYearMonth(String yearMonth);
    void deleteByYearMonth(String yearMonth);
}
