package cl.duoc.bancoxyz.mobile.service;

import cl.duoc.bancoxyz.mobile.dto.MobileAccountResponse;
import cl.duoc.bancoxyz.mobile.dto.MobileMovementResponse;
import cl.duoc.bancoxyz.mobile.exception.ResourceNotFoundException;
import cl.duoc.bancoxyz.mobile.model.MobileAccount;
import cl.duoc.bancoxyz.mobile.repository.MobileDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MobileQueryService {

    private final MobileDataRepository repository;

    public MobileQueryService(MobileDataRepository repository) {
        this.repository = repository;
    }

    public MobileAccountResponse getSummary(long accountId) {
        MobileAccount account = requireAccount(accountId);
        return new MobileAccountResponse(account.accountId(), account.type(), account.balance());
    }

    public List<MobileMovementResponse> getRecentMovements(long accountId, int limit) {
        requireAccount(accountId);
        if (limit < 1 || limit > 10) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y 10");
        }
        return repository.findMovements(accountId).stream()
                .limit(limit)
                .map(movement -> new MobileMovementResponse(
                        movement.date(),
                        movement.type(),
                        movement.amount()))
                .toList();
    }

    private MobileAccount requireAccount(long accountId) {
        if (accountId <= 0) {
            throw new IllegalArgumentException("El identificador de cuenta debe ser mayor que cero");
        }
        return repository.findAccount(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la cuenta " + accountId));
    }
}
