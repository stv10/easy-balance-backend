package stv10.mb2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import stv10.mb2.dto.DashboardSummaryDTO;
import stv10.mb2.dto.MonthlyBudgetConfigDTO;
import stv10.mb2.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/summary")
    public DashboardSummaryDTO getSummary(@RequestParam(required = false) String yearMonth) {
        return service.getSummary(yearMonth);
    }

    @PostMapping("/generate")
    public DashboardSummaryDTO generateMonthlyExpenses(@RequestParam String yearMonth) {
        return service.generateMonthlyExpenses(yearMonth);
    }

    @PostMapping("/regenerate")
    public DashboardSummaryDTO regenerateMonthlyExpenses(@RequestParam String yearMonth) {
        return service.regenerateMonthlyExpenses(yearMonth);
    }

    @PutMapping("/budget")
    public DashboardSummaryDTO updateMonthlyBudget(@RequestParam String yearMonth,
                                                   @RequestBody MonthlyBudgetConfigDTO config) {
        return service.updateMonthlyBudget(yearMonth, config);
    }

    @PostMapping("/budget/reset")
    public DashboardSummaryDTO resetMonthlyBudget(@RequestParam String yearMonth) {
        return service.resetMonthlyBudget(yearMonth);
    }
}

