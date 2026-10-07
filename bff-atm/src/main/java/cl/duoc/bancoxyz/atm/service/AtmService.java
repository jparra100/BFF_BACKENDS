package cl.duoc.bancoxyz.atm.service;

import cl.duoc.bancoxyz.atm.client.AtmBankingClient;
import cl.duoc.bancoxyz.atm.dto.BalanceResponse;
import cl.duoc.bancoxyz.atm.dto.WithdrawalResponse;
import cl.duoc.bancoxyz.atm.exception.AccountNotFoundException;
import cl.duoc.bancoxyz.atm.exception.InsufficientFundsException;
import cl.duoc.bancoxyz.atm.model.AtmAccount;
import cl.duoc.bancoxyz.atm.repository.AtmAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class AtmService {

    private final AtmAccountRepository repository;
    private final Optional<AtmBankingClient> bankingClient;
    private final boolean remoteServicesEnabled;

    public AtmService(AtmAccountRepository repository) {
        this(repository, Optional.empty(), false);
    }

    @Autowired
    public AtmService(
            AtmAccountRepository repository,
            Optional<AtmBankingClient> bankingClient,
            @Value("${app.remote-services.enabled:false}") boolean remoteServicesEnabled) {
        this.repository = repository;
        this.bankingClient = bankingClient;
        this.remoteServicesEnabled = remoteServicesEnabled;
    }

    public BalanceResponse getBalance(long accountId) {
        validateAccountId(accountId);
        if (remoteServicesEnabled) {
            return remoteClient().getBalance(accountId);
        }
        AtmAccount account = requireAccount(accountId);
        return new BalanceResponse(account.accountId(), account.balance());
    }

    public WithdrawalResponse withdraw(long accountId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }

        validateAccountId(accountId);
        if (remoteServicesEnabled) {
            return remoteClient().withdraw(accountId, amount);
        }

        AtmAccount account = requireAccount(accountId);
        BigDecimal resultingBalance = account.withdraw(amount)
                .orElseThrow(InsufficientFundsException::new);

        return new WithdrawalResponse(account.accountId(), amount, resultingBalance);
    }

    private AtmAccount requireAccount(long accountId) {
        validateAccountId(accountId);
        return repository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private void validateAccountId(long accountId) {
        if (accountId <= 0) {
            throw new IllegalArgumentException("El identificador de cuenta debe ser mayor que cero");
        }
    }

    private AtmBankingClient remoteClient() {
        return bankingClient.orElseThrow(() -> new IllegalStateException(
                "La integración remota está habilitada, pero el cliente no está disponible"));
    }
}
