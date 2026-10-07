package cl.duoc.bancoxyz.web.client;

import cl.duoc.bancoxyz.web.dto.PageResponse;
import cl.duoc.bancoxyz.web.dto.WebAccountDetailResponse;
import cl.duoc.bancoxyz.web.dto.WebAccountResponse;
import cl.duoc.bancoxyz.web.dto.WebMovementResponse;
import cl.duoc.bancoxyz.web.dto.WebTransactionResponse;
import cl.duoc.bancoxyz.web.exception.RemoteServiceException;
import cl.duoc.bancoxyz.web.exception.ResourceNotFoundException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@Component
@ConditionalOnProperty(name = "app.remote-services.enabled", havingValue = "true")
public class WebBankingClient {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Santiago");

    private final RestClient restClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public WebBankingClient(RestClient.Builder builder, CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.restClient = builder.build();
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public PageResponse<WebAccountResponse> findAccounts(String type, int page, int size) {
        String normalizedType = normalize(type);
        List<WebAccountResponse> accounts = accounts().stream()
                .filter(account -> normalizedType == null || normalizedType.equals(account.type()))
                .map(account -> {
                    RemoteCustomer customer = customer(account.customerId());
                    return new WebAccountResponse(account.id(), customer.name(), 0, account.type(), account.balance());
                })
                .toList();
        return PageResponse.from(accounts, page, size);
    }

    public WebAccountDetailResponse findAccount(long accountId) {
        RemoteAccount account = account(accountId);
        RemoteCustomer customer = customer(account.customerId());
        List<WebMovementResponse> movements = movementsFor(accountId);
        return new WebAccountDetailResponse(
                account.id(), customer.name(), 0, account.type(), account.balance(), movements);
    }

    public PageResponse<WebMovementResponse> findMovements(long accountId, int page, int size) {
        account(accountId);
        return PageResponse.from(movementsFor(accountId), page, size);
    }

    public PageResponse<WebTransactionResponse> findTransactions(
            String type, LocalDate from, LocalDate to, int page, int size) {
        String normalizedType = normalize(type);
        List<WebTransactionResponse> transactions = payments().stream()
                .filter(payment -> normalizedType == null || normalizedType.equals(payment.type()))
                .filter(payment -> from == null || !payment.createdAt().atZone(BUSINESS_ZONE).toLocalDate().isBefore(from))
                .filter(payment -> to == null || !payment.createdAt().atZone(BUSINESS_ZONE).toLocalDate().isAfter(to))
                .map(payment -> new WebTransactionResponse(
                        Math.abs(payment.id().getMostSignificantBits()),
                        payment.createdAt().atZone(BUSINESS_ZONE).toLocalDate(),
                        payment.type(),
                        payment.amount()))
                .toList();
        return PageResponse.from(transactions, page, size);
    }

    private List<WebMovementResponse> movementsFor(long accountId) {
        return payments().stream()
                .filter(payment -> Long.valueOf(accountId).equals(payment.sourceAccountId())
                        || Long.valueOf(accountId).equals(payment.targetAccountId()))
                .map(payment -> new WebMovementResponse(
                        payment.createdAt().atZone(BUSINESS_ZONE).toLocalDate(),
                        payment.type(),
                        signedAmount(payment, accountId),
                        "Operación " + payment.status().toLowerCase(Locale.ROOT)))
                .toList();
    }

    private BigDecimal signedAmount(RemotePayment payment, long accountId) {
        return Long.valueOf(accountId).equals(payment.sourceAccountId())
                ? payment.amount().negate()
                : payment.amount();
    }

    private List<RemoteAccount> accounts() {
        return execute("accounts", () -> restClient.get()
                .uri("http://account-service/api/accounts")
                .headers(forwardAuthorization())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {}));
    }

    private RemoteAccount account(long accountId) {
        return execute("accounts", () -> restClient.get()
                .uri("http://account-service/api/accounts/{id}", accountId)
                .headers(forwardAuthorization())
                .retrieve()
                .body(RemoteAccount.class));
    }

    private RemoteCustomer customer(long customerId) {
        return execute("customers", () -> restClient.get()
                .uri("http://customer-service/api/customers/{id}", customerId)
                .headers(forwardAuthorization())
                .retrieve()
                .body(RemoteCustomer.class));
    }

    private List<RemotePayment> payments() {
        return execute("payments", () -> restClient.get()
                .uri("http://payment-service/api/payments")
                .headers(forwardAuthorization())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {}));
    }

    private <T> T execute(String circuitName, java.util.function.Supplier<T> operation) {
        try {
            return circuitBreakerFactory.create(circuitName).run(operation,
                    error -> { throw mapError(error); });
        } catch (ResourceNotFoundException | RemoteServiceException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw mapError(ex);
        }
    }

    private Consumer<HttpHeaders> forwardAuthorization() {
        return headers -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwt) {
                headers.setBearerAuth(jwt.getToken().getTokenValue());
            }
        };
    }

    private RuntimeException mapError(Throwable error) {
        if (error instanceof RestClientResponseException response
                && response.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new ResourceNotFoundException("El recurso solicitado no existe");
        }
        return new RemoteServiceException("No fue posible consultar los servicios bancarios", error);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    public record RemoteAccount(Long id, Long customerId, String type, BigDecimal balance, String status) {}
    public record RemoteCustomer(Long id, String name, String email, String profile) {}
    public record RemotePayment(
            java.util.UUID id,
            String operationId,
            Long sourceAccountId,
            Long targetAccountId,
            BigDecimal amount,
            String type,
            String status,
            java.time.Instant createdAt) {}
}
