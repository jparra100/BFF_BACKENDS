package cl.duoc.bancoxyz.mobile.service;

import cl.duoc.bancoxyz.mobile.client.MobileBankingClient;
import cl.duoc.bancoxyz.mobile.dto.MobileAccountResponse;
import cl.duoc.bancoxyz.mobile.dto.MobileMovementResponse;
import cl.duoc.bancoxyz.mobile.exception.ResourceNotFoundException;
import cl.duoc.bancoxyz.mobile.model.MobileAccount;
import cl.duoc.bancoxyz.mobile.repository.MobileDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Optional;

@Service
public class MobileQueryService {

    private final MobileDataRepository repository;
    private final Optional<MobileBankingClient> bankingClient;
    private final boolean remoteServicesEnabled;

    public MobileQueryService(MobileDataRepository repository) {
        this(repository, Optional.empty(), false);
    }

    @Autowired
    public MobileQueryService(
            MobileDataRepository repository,
            Optional<MobileBankingClient> bankingClient,
            @Value("${app.remote-services.enabled:false}") boolean remoteServicesEnabled) {
        this.repository = repository;
        this.bankingClient = bankingClient;
        this.remoteServicesEnabled = remoteServicesEnabled;
    }

    public MobileAccountResponse getSummary(long accountId) {
        validateAccountId(accountId);
        if (remoteServicesEnabled) {
            return remoteClient().getSummary(accountId);
        }
        MobileAccount account = requireAccount(accountId);
        return new MobileAccountResponse(account.accountId(), account.type(), account.balance());
    }

    public List<MobileMovementResponse> getRecentMovements(long accountId, int limit) {
        validateAccountId(accountId);
        if (limit < 1 || limit > 10) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y 10");
        }
        if (remoteServicesEnabled) {
            return remoteClient().getRecentMovements(accountId, limit);
        }
        requireAccount(accountId);
        return repository.findMovements(accountId).stream()
                .limit(limit)
                .map(movement -> new MobileMovementResponse(
                        movement.date(),
                        movement.type(),
                        movement.amount()))
                .toList();
    }

    private MobileAccount requireAccount(long accountId) {
        validateAccountId(accountId);
        return repository.findAccount(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la cuenta " + accountId));
    }

    private void validateAccountId(long accountId) {
        if (accountId <= 0) {
            throw new IllegalArgumentException("El identificador de cuenta debe ser mayor que cero");
        }
    }

    private MobileBankingClient remoteClient() {
        return bankingClient.orElseThrow(() -> new IllegalStateException(
                "La integración remota está habilitada, pero el cliente no está disponible"));
    }
}
