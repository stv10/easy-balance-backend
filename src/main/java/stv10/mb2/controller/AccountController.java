package stv10.mb2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import stv10.mb2.model.Account;
import stv10.mb2.repository.AccountRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository repository;

    @GetMapping
    public List<Account> getAccounts() {
        return repository.findAll();
    }

    @PostMapping
    public Account addAccount(@RequestBody Account account) {
        return repository.save(account);
    }

    @PutMapping("/{id}")
    public Account updateAccount(@PathVariable UUID id, @RequestBody Account account) {
        account.setId(id);
        return repository.save(account);
    }

    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable UUID id) {
        repository.deleteById(id);
    }
}
