package stv10.mb2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stv10.mb2.model.FixedExpense;
import java.util.List;
import java.util.UUID;

public interface FixedExpenseRepository extends JpaRepository<FixedExpense, UUID> {
    List<FixedExpense> findByDueDay(Integer dueDay);

    @Modifying
    @Query("UPDATE FixedExpense fe SET fe.tag = null WHERE fe.tag.id = :tagId")
    void unlinkTag(@Param("tagId") UUID tagId);
}
