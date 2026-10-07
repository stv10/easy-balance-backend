package stv10.mb2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import stv10.mb2.dto.BulkUpdateExpenseTagRequest;
import stv10.mb2.dto.MonthlyExpensesAnalyticsDTO;
import stv10.mb2.dto.SSPRequest;
import stv10.mb2.dto.SSPResponse;
import stv10.mb2.dto.TagMonthHistoryDTO;
import stv10.mb2.dto.UpdateExpenseTagRequest;
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

    @GetMapping("/analytics/monthly")
    public MonthlyExpensesAnalyticsDTO getMonthlyAnalytics(@RequestParam(required = false) String yearMonth) {
        return service.getMonthlyAnalytics(yearMonth);
    }

    @GetMapping("/analytics/tag-history")
    public List<TagMonthHistoryDTO> getTagHistory(
            @RequestParam(required = false) UUID tagId,
            @RequestParam(required = false) String yearMonth) {
        return service.getTagHistory(tagId, yearMonth);
    }

    @GetMapping("/by-tag")
    public List<Expense> getExpensesByTag(
            @RequestParam(required = false) UUID tagId,
            @RequestParam(required = false) String yearMonth) {
        return service.getExpensesByTag(tagId, yearMonth);
    }

    @RequestMapping(value = "/{id}/tag", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public Expense updateExpenseTag(@PathVariable UUID id, @RequestBody(required = false) UpdateExpenseTagRequest request) {
        UUID tagId = request != null ? request.getTagId() : null;
        return service.updateExpenseTag(id, tagId);
    }

    @RequestMapping(value = "/bulk-tag", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public List<Expense> bulkUpdateExpenseTag(@RequestBody BulkUpdateExpenseTagRequest request) {
        return service.bulkUpdateExpenseTag(request.getExpenseIds(), request.getTagId());
    }
}
