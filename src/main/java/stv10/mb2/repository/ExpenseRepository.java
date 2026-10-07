package stv10.mb2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stv10.mb2.model.Expense;
import stv10.mb2.model.ExpenseCategory;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID>, JpaSpecificationExecutor<Expense> {
    List<Expense> findByCategory(ExpenseCategory category);
    List<Expense> findByAccountId(UUID accountId);
    List<Expense> findByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    List<Expense> findByFixedExpenseId(UUID fixedExpenseId);
    @Query("SELECT e FROM Expense e WHERE e.tag.id = :tagId AND e.createdAt BETWEEN :start AND :end ORDER BY e.createdAt DESC")
    List<Expense> findByTagIdAndCreatedAtBetween(@Param("tagId") UUID tagId, @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    @Query("SELECT e FROM Expense e WHERE e.tag IS NULL AND e.createdAt BETWEEN :start AND :end ORDER BY e.createdAt DESC")
    List<Expense> findByTagIsNullAndCreatedAtBetween(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    @Modifying
    @Query("UPDATE Expense e SET e.tag = null WHERE e.tag.id = :tagId")
    void unlinkTag(@Param("tagId") UUID tagId);
}
