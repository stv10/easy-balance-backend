package stv10.mb2.strategy;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;
import stv10.mb2.model.Expense;
import java.util.Optional;
import java.util.UUID;

@Component
public class AccountFilterStrategy implements ExpenseFilterStrategy {
    @Override
    public boolean supports(String key) {
        return "accountId".equalsIgnoreCase(key);
    }

    @Override
    public Predicate apply(Root<Expense> root, CriteriaQuery<?> query, CriteriaBuilder cb, String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(val -> !val.isEmpty())
                .map(val -> {
                    try {
                        UUID accountId = UUID.fromString(val);
                        return cb.equal(root.get("accountId"), accountId);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("ID de cuenta inválido: " + val);
                    }
                })
                .orElseGet(cb::conjunction);
    }
}
