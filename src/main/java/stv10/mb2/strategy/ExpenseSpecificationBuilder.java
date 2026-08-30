package stv10.mb2.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import stv10.mb2.dto.SSPFilter;
import stv10.mb2.model.Expense;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExpenseSpecificationBuilder {

    private final List<ExpenseFilterStrategy> strategies;

    public Specification<Expense> build(List<SSPFilter> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Optional.ofNullable(filters).orElse(List.of()).forEach(filter -> {
                Optional.ofNullable(filter.getValue())
                        .map(String::trim)
                        .filter(val -> !val.isEmpty())
                        .ifPresent(val -> strategies.stream()
                                .filter(strategy -> strategy.supports(filter.getKey()))
                                .findFirst()
                                .ifPresent(strategy -> predicates.add(strategy.apply(root, query, cb, val))));
            });
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
