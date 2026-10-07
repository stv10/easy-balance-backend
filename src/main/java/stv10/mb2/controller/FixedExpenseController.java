package stv10.mb2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import stv10.mb2.dto.CreateFixedExpenseDTO;
import stv10.mb2.dto.UpdateFixedExpenseDTO;
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
    public FixedExpense addFixedExpense(@RequestBody CreateFixedExpenseDTO dto) {
        FixedExpense fixedExpense = new FixedExpense();
        fixedExpense.setDescription(dto.description());
        fixedExpense.setAmount(dto.amount());
        fixedExpense.setCategory(dto.category());
        fixedExpense.setDueDay(dto.dueDay());
        if (dto.tagId() != null) {
            fixedExpense.setTag(tagRepository.findById(dto.tagId()).orElse(null));
        } else {
            fixedExpense.setTag(null);
        }
        return repository.save(fixedExpense);
    }

    @PutMapping("/{id}")
    public FixedExpense updateFixedExpense(@PathVariable UUID id, @RequestBody UpdateFixedExpenseDTO dto) {
        FixedExpense fixedExpense = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fixed expense not found: " + id));
        fixedExpense.setDescription(dto.description());
        fixedExpense.setAmount(dto.amount());
        fixedExpense.setCategory(dto.category());
        fixedExpense.setDueDay(dto.dueDay());
        if (dto.tagId() != null) {
            fixedExpense.setTag(tagRepository.findById(dto.tagId()).orElse(null));
        } else {
            fixedExpense.setTag(null);
        }
        return repository.save(fixedExpense);
    }

    @DeleteMapping("/{id}")
    public void deleteFixedExpense(@PathVariable UUID id) {
        repository.deleteById(id);
    }
}
