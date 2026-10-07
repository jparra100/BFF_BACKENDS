package cl.duoc.bancoxyz.accounts.controller;

import cl.duoc.bancoxyz.accounts.dto.AccountRequest;
import cl.duoc.bancoxyz.accounts.dto.AccountResponse;
import cl.duoc.bancoxyz.accounts.dto.BalanceChangeRequest;
import cl.duoc.bancoxyz.accounts.service.BankAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final BankAccountService service;
    public AccountController(BankAccountService service) { this.service = service; }

    @GetMapping public List<AccountResponse> all() { return service.findAll(); }
    @GetMapping("/{id}") public AccountResponse one(@PathVariable long id) { return service.find(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public AccountResponse open(@Valid @RequestBody AccountRequest request) { return service.open(request); }
    @PostMapping("/{id}/close") public AccountResponse close(@PathVariable long id) { return service.close(id); }
    @PostMapping("/{id}/credits") public AccountResponse credit(@PathVariable long id, @Valid @RequestBody BalanceChangeRequest request) { return service.credit(id, request.amount()); }
    @PostMapping("/{id}/debits") public AccountResponse debit(@PathVariable long id, @Valid @RequestBody BalanceChangeRequest request) { return service.debit(id, request.amount()); }
}
