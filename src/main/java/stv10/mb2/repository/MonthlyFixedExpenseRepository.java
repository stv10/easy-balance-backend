package stv10.mb2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stv10.mb2.model.MonthlyFixedExpense;
import java.util.List;
import java.util.UUID;

public interface MonthlyFixedExpenseRepository extends JpaRepository<MonthlyFixedExpense, UUID> {
    List<MonthlyFixedExpense> findByYearMonth(String yearMonth);
    boolean existsByYearMonth(String yearMonth);
    void deleteByYearMonth(String yearMonth);

    @Modifying
    @Query("UPDATE MonthlyFixedExpense mfe SET mfe.tag = null WHERE mfe.tag.id = :tagId")
    void unlinkTag(@Param("tagId") UUID tagId);
}
