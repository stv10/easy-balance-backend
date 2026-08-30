package stv10.mb2.strategy;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;
import stv10.mb2.model.Expense;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Component
public class DateToFilterStrategy implements ExpenseFilterStrategy {
    @Override
    public boolean supports(String key) {
        return "dateTo".equalsIgnoreCase(key) || "fechasHasta".equalsIgnoreCase(key);
    }

    @Override
    public Predicate apply(Root<Expense> root, CriteriaQuery<?> query, CriteriaBuilder cb, String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(val -> !val.isEmpty())
                .map(val -> {
                    try {
                        LocalDateTime dateTime;
                        if (val.length() == 10) { // yyyy-MM-dd
                            dateTime = LocalDate.parse(val).atTime(LocalTime.MAX);
                        } else {
                            dateTime = LocalDateTime.parse(val);
                        }
                        return cb.lessThanOrEqualTo(root.get("createdAt"), dateTime);
                    } catch (DateTimeParseException e) {
                        throw new IllegalArgumentException("Fecha 'hasta' inválida: " + val);
                    }
                })
                .orElseGet(cb::conjunction);
    }
}
