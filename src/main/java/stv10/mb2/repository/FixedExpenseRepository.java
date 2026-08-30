package stv10.mb2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stv10.mb2.model.FixedExpense;
import java.util.List;
import java.util.UUID;

public interface FixedExpenseRepository extends JpaRepository<FixedExpense, UUID> {
    List<FixedExpense> findByDueDay(Integer dueDay);
}
