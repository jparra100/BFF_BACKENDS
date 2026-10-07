package cl.duoc.bancoxyz.accounts.service;

import cl.duoc.bancoxyz.accounts.dto.AccountRequest;
import cl.duoc.bancoxyz.accounts.dto.AccountResponse;
import cl.duoc.bancoxyz.accounts.model.BankAccount;
import cl.duoc.bancoxyz.accounts.repository.BankAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class BankAccountService {
    private final BankAccountRepository repository;

    public BankAccountService(BankAccountRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<AccountResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }

    @Transactional(readOnly = true)
    public AccountResponse find(long id) { return toResponse(require(id)); }

    public AccountResponse open(AccountRequest request) {
        BankAccount saved = repository.save(new BankAccount(request.customerId(), normalizedType(request.type()), request.initialBalance()));
        return toResponse(saved);
    }

    public AccountResponse close(long id) {
        BankAccount account = require(id);
        account.close();
        return toResponse(account);
    }

    public AccountResponse credit(long id, BigDecimal amount) { BankAccount account = require(id); account.credit(amount); return toResponse(account); }
    public AccountResponse debit(long id, BigDecimal amount) { BankAccount account = require(id); account.debit(amount); return toResponse(account); }

    private BankAccount require(long id) { return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada")); }
    private String normalizedType(String value) { return value.trim().toUpperCase(Locale.ROOT); }
    private AccountResponse toResponse(BankAccount a) { return new AccountResponse(a.getId(), a.getCustomerId(), a.getType(), a.getBalance(), a.getStatus()); }
}
