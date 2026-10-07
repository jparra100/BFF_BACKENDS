package cl.duoc.bancoxyz.web.service;

import cl.duoc.bancoxyz.web.client.WebBankingClient;
import cl.duoc.bancoxyz.web.dto.PageResponse;
import cl.duoc.bancoxyz.web.dto.WebAccountDetailResponse;
import cl.duoc.bancoxyz.web.dto.WebAccountResponse;
import cl.duoc.bancoxyz.web.dto.WebMovementResponse;
import cl.duoc.bancoxyz.web.dto.WebTransactionResponse;
import cl.duoc.bancoxyz.web.exception.ResourceNotFoundException;
import cl.duoc.bancoxyz.web.model.Account;
import cl.duoc.bancoxyz.web.model.AnnualMovement;
import cl.duoc.bancoxyz.web.model.LegacyTransaction;
import cl.duoc.bancoxyz.web.repository.WebDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class WebQueryService {

    private final WebDataRepository repository;
    private final Optional<WebBankingClient> bankingClient;
    private final boolean remoteServicesEnabled;

    public WebQueryService(WebDataRepository repository) {
        this(repository, Optional.empty(), false);
    }

    @Autowired
    public WebQueryService(
            WebDataRepository repository,
            Optional<WebBankingClient> bankingClient,
            @Value("${app.remote-services.enabled:false}") boolean remoteServicesEnabled) {
        this.repository = repository;
        this.bankingClient = bankingClient;
        this.remoteServicesEnabled = remoteServicesEnabled;
    }

    public PageResponse<WebAccountResponse> findAccounts(String type, int page, int size) {
        if (remoteServicesEnabled) {
            return remoteClient().findAccounts(type, page, size);
        }
        String normalizedType = normalizeFilter(type);
        List<WebAccountResponse> accounts = repository.findAllAccounts().stream()
                .filter(account -> normalizedType == null || account.accountType().equals(normalizedType))
                .map(this::toAccountResponse)
                .toList();
        return PageResponse.from(accounts, page, size);
    }

    public WebAccountDetailResponse findAccount(long accountId) {
        if (remoteServicesEnabled) {
            return remoteClient().findAccount(accountId);
        }
        Account account = requireAccount(accountId);
        List<WebMovementResponse> movements = repository.findMovements(accountId).stream()
                .map(this::toMovementResponse)
                .toList();
        return new WebAccountDetailResponse(
                account.accountId(),
                account.holderName(),
                account.holderAge(),
                account.accountType(),
                account.balance(),
                movements
        );
    }

    public PageResponse<WebMovementResponse> findMovements(long accountId, int page, int size) {
        if (remoteServicesEnabled) {
            return remoteClient().findMovements(accountId, page, size);
        }
        requireAccount(accountId);
        List<WebMovementResponse> movements = repository.findMovements(accountId).stream()
                .map(this::toMovementResponse)
                .toList();
        return PageResponse.from(movements, page, size);
    }

    public PageResponse<WebTransactionResponse> findTransactions(
            String type,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta");
        }

        if (remoteServicesEnabled) {
            return remoteClient().findTransactions(type, from, to, page, size);
        }

        String normalizedType = normalizeFilter(type);
        List<WebTransactionResponse> transactions = repository.findAllTransactions().stream()
                .filter(transaction -> normalizedType == null || transaction.type().equals(normalizedType))
                .filter(transaction -> from == null || !transaction.date().isBefore(from))
                .filter(transaction -> to == null || !transaction.date().isAfter(to))
                .map(this::toTransactionResponse)
                .toList();
        return PageResponse.from(transactions, page, size);
    }

    private Account requireAccount(long accountId) {
        return repository.findAccount(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la cuenta " + accountId));
    }

    private WebAccountResponse toAccountResponse(Account account) {
        return new WebAccountResponse(
                account.accountId(),
                account.holderName(),
                account.holderAge(),
                account.accountType(),
                account.balance()
        );
    }

    private WebMovementResponse toMovementResponse(AnnualMovement movement) {
        return new WebMovementResponse(
                movement.date(),
                movement.type(),
                movement.amount(),
                movement.description()
        );
    }

    private WebTransactionResponse toTransactionResponse(LegacyTransaction transaction) {
        return new WebTransactionResponse(
                transaction.id(),
                transaction.date(),
                transaction.type(),
                transaction.amount()
        );
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private WebBankingClient remoteClient() {
        return bankingClient.orElseThrow(() -> new IllegalStateException(
                "La integración remota está habilitada, pero el cliente no está disponible"));
    }
}

