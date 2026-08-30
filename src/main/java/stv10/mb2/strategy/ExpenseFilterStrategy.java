package stv10.mb2.strategy;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import stv10.mb2.model.Expense;

public interface ExpenseFilterStrategy {
    boolean supports(String key);
    Predicate apply(Root<Expense> root, CriteriaQuery<?> query, CriteriaBuilder cb, String value);
}
