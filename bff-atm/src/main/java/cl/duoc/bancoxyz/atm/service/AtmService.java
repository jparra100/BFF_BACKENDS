package cl.duoc.bancoxyz.atm.service;

import cl.duoc.bancoxyz.atm.dto.BalanceResponse;
import cl.duoc.bancoxyz.atm.dto.WithdrawalResponse;
import cl.duoc.bancoxyz.atm.exception.AccountNotFoundException;
import cl.duoc.bancoxyz.atm.exception.InsufficientFundsException;
import cl.duoc.bancoxyz.atm.model.AtmAccount;
import cl.duoc.bancoxyz.atm.repository.AtmAccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AtmService {

    private final AtmAccountRepository repository;

    public AtmService(AtmAccountRepository repository) {
        this.repository = repository;
    }

    public BalanceResponse getBalance(long accountId) {
        AtmAccount account = requireAccount(accountId);
        return new BalanceResponse(account.accountId(), account.balance());
    }

    public WithdrawalResponse withdraw(long accountId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }

        AtmAccount account = requireAccount(accountId);
        BigDecimal resultingBalance = account.withdraw(amount)
                .orElseThrow(InsufficientFundsException::new);

        return new WithdrawalResponse(account.accountId(), amount, resultingBalance);
    }

    private AtmAccount requireAccount(long accountId) {
        if (accountId <= 0) {
            throw new IllegalArgumentException("El identificador de cuenta debe ser mayor que cero");
        }
        return repository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}
