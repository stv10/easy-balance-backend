package stv10.mb2.strategy;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;
import stv10.mb2.model.Expense;
import stv10.mb2.model.ExpenseCategory;
import java.util.Optional;

@Component
public class CategoryFilterStrategy implements ExpenseFilterStrategy {
    @Override
    public boolean supports(String key) {
        return "category".equalsIgnoreCase(key);
    }

    @Override
    public Predicate apply(Root<Expense> root, CriteriaQuery<?> query, CriteriaBuilder cb, String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(val -> !val.isEmpty())
                .map(val -> {
                    try {
                        ExpenseCategory category = ExpenseCategory.valueOf(val.toUpperCase());
                        return cb.equal(root.get("category"), category);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Categoría inválida: " + val);
                    }
                })
                .orElseGet(cb::conjunction);
    }
}
