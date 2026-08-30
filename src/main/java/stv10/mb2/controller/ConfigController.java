package stv10.mb2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import stv10.mb2.model.BudgetConfig;
import stv10.mb2.repository.BudgetConfigRepository;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final BudgetConfigRepository repository;

    @GetMapping
    public BudgetConfig getConfig() {
        return repository.findAll().stream()
                .findFirst()
                .orElseGet(BudgetConfig::new);
    }

    @PutMapping
    public BudgetConfig updateConfig(@RequestBody BudgetConfig config) {
        repository.findAll().stream()
                .findFirst()
                .ifPresent(existing -> config.setId(existing.getId()));
        return repository.save(config);
    }
}
