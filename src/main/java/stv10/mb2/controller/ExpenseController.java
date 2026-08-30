package stv10.mb2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import stv10.mb2.dto.SSPRequest;
import stv10.mb2.dto.SSPResponse;
import stv10.mb2.model.Expense;
import stv10.mb2.service.ExpenseService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService service;

    @GetMapping
    public List<Expense> getExpenses() {
        return service.getAllExpenses();
    }

    @PostMapping
    public Expense addExpense(@RequestBody Expense expense) {
        return service.addExpense(expense);
    }

    @PostMapping("/ssp")
    public SSPResponse<Expense> getExpensesSSP(@RequestBody SSPRequest request) {
        return service.getExpensesSSP(request);
    }

    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable UUID id) {
        service.deleteExpense(id);
    }
}
