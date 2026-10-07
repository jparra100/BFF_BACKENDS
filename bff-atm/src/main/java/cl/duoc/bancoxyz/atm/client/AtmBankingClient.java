package cl.duoc.bancoxyz.atm.client;

import cl.duoc.bancoxyz.atm.dto.BalanceResponse;
import cl.duoc.bancoxyz.atm.dto.WithdrawalResponse;
import cl.duoc.bancoxyz.atm.exception.AccountNotFoundException;
import cl.duoc.bancoxyz.atm.exception.InsufficientFundsException;
import cl.duoc.bancoxyz.atm.exception.RemoteServiceException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

@Component
@ConditionalOnProperty(name = "app.remote-services.enabled", havingValue = "true")
public class AtmBankingClient {

    private final RestClient restClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public AtmBankingClient(RestClient.Builder builder, CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.restClient = builder.build();
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public BalanceResponse getBalance(long accountId) {
        RemoteAccount account = findAccount(accountId);
        return new BalanceResponse(account.id(), account.balance());
    }

    public WithdrawalResponse withdraw(long accountId, BigDecimal amount) {
        RemoteAccount account = findAccount(accountId);
        if (account.balance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        PaymentRequest request = new PaymentRequest(
                "ATM-" + UUID.randomUUID(), accountId, null, amount, "WITHDRAWAL");
        execute("payments", () -> restClient.post()
                .uri("http://payment-service/api/payments")
                .headers(forwardAuthorization())
                .body(request)
                .retrieve()
                .body(PaymentResponse.class));

        return new WithdrawalResponse(accountId, amount, account.balance().subtract(amount));
    }

    private RemoteAccount findAccount(long accountId) {
        return execute("accounts", () -> restClient.get()
                .uri("http://account-service/api/accounts/{id}", accountId)
                .headers(forwardAuthorization())
                .retrieve()
                .body(RemoteAccount.class));
    }

    private Consumer<HttpHeaders> forwardAuthorization() {
        return headers -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwt) {
                headers.setBearerAuth(jwt.getToken().getTokenValue());
            }
        };
    }

    private <T> T execute(String circuitName, java.util.function.Supplier<T> operation) {
        try {
            return circuitBreakerFactory.create(circuitName).run(operation,
                    error -> { throw mapError(error); });
        } catch (AccountNotFoundException | InsufficientFundsException | RemoteServiceException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw mapError(ex);
        }
    }

    private RuntimeException mapError(Throwable error) {
        if (error instanceof RestClientResponseException response
                && response.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new AccountNotFoundException();
        }
        return new RemoteServiceException("No fue posible completar la operación del cajero", error);
    }

    public record RemoteAccount(Long id, Long customerId, String type, BigDecimal balance, String status) {}
    public record PaymentRequest(
            String operationId, Long sourceAccountId, Long targetAccountId, BigDecimal amount, String type) {}
    public record PaymentResponse(
            UUID id, String operationId, Long sourceAccountId, Long targetAccountId,
            BigDecimal amount, String type, String status, Instant createdAt) {}
}
