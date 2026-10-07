package stv10.mb2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import stv10.mb2.model.FixedExpense;
import stv10.mb2.repository.FixedExpenseRepository;

import stv10.mb2.repository.TagRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fixed-expenses")
@RequiredArgsConstructor
public class FixedExpenseController {

    private final FixedExpenseRepository repository;
    private final TagRepository tagRepository;

    @GetMapping
    public List<FixedExpense> getFixedExpenses() {
        return repository.findAll();
    }

    @PostMapping
    public FixedExpense addFixedExpense(@RequestBody FixedExpense fixedExpense) {
        resolveTag(fixedExpense);
        return repository.save(fixedExpense);
    }

    @PutMapping("/{id}")
    public FixedExpense updateFixedExpense(@PathVariable UUID id, @RequestBody FixedExpense fixedExpense) {
        fixedExpense.setId(id);
        resolveTag(fixedExpense);
        return repository.save(fixedExpense);
    }

    private void resolveTag(FixedExpense fixedExpense) {
        if (fixedExpense.getTag() != null && fixedExpense.getTag().getId() != null) {
            fixedExpense.setTag(tagRepository.findById(fixedExpense.getTag().getId()).orElse(null));
        } else {
            fixedExpense.setTag(null);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteFixedExpense(@PathVariable UUID id) {
        repository.deleteById(id);
    }
}
