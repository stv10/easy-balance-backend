package stv10.mb2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stv10.mb2.model.MonthlyFixedExpense;
import java.util.List;
import java.util.UUID;

public interface MonthlyFixedExpenseRepository extends JpaRepository<MonthlyFixedExpense, UUID> {
    List<MonthlyFixedExpense> findByYearMonth(String yearMonth);
    boolean existsByYearMonth(String yearMonth);
    void deleteByYearMonth(String yearMonth);
}
