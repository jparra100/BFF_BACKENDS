package cl.duoc.bancoxyz.mobile.client;

import cl.duoc.bancoxyz.mobile.dto.MobileAccountResponse;
import cl.duoc.bancoxyz.mobile.dto.MobileMovementResponse;
import cl.duoc.bancoxyz.mobile.exception.RemoteServiceException;
import cl.duoc.bancoxyz.mobile.exception.ResourceNotFoundException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Component
@ConditionalOnProperty(name = "app.remote-services.enabled", havingValue = "true")
public class MobileBankingClient {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Santiago");

    private final RestClient restClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public MobileBankingClient(RestClient.Builder builder, CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.restClient = builder.build();
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public MobileAccountResponse getSummary(long accountId) {
        RemoteAccount account = findAccount(accountId);
        return new MobileAccountResponse(account.id(), account.type(), account.balance());
    }

    public List<MobileMovementResponse> getRecentMovements(long accountId, int limit) {
        findAccount(accountId);
        return payments().stream()
                .filter(payment -> Long.valueOf(accountId).equals(payment.sourceAccountId())
                        || Long.valueOf(accountId).equals(payment.targetAccountId()))
                .sorted((left, right) -> right.createdAt().compareTo(left.createdAt()))
                .limit(limit)
                .map(payment -> new MobileMovementResponse(
                        payment.createdAt().atZone(BUSINESS_ZONE).toLocalDate(),
                        payment.type(),
                        signedAmount(payment, accountId)))
                .toList();
    }

    private RemoteAccount findAccount(long accountId) {
        return execute("accounts", () -> restClient.get()
                .uri("http://account-service/api/accounts/{id}", accountId)
                .headers(forwardAuthorization())
                .retrieve()
                .body(RemoteAccount.class));
    }

    private List<RemotePayment> payments() {
        return execute("payments", () -> restClient.get()
                .uri("http://payment-service/api/payments")
                .headers(forwardAuthorization())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {}));
    }

    private BigDecimal signedAmount(RemotePayment payment, long accountId) {
        return Long.valueOf(accountId).equals(payment.sourceAccountId())
                ? payment.amount().negate()
                : payment.amount();
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
        } catch (ResourceNotFoundException | RemoteServiceException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw mapError(ex);
        }
    }

    private RuntimeException mapError(Throwable error) {
        if (error instanceof RestClientResponseException response
                && response.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new ResourceNotFoundException("La cuenta solicitada no existe");
        }
        return new RemoteServiceException("No fue posible consultar la información móvil", error);
    }

    public record RemoteAccount(Long id, Long customerId, String type, BigDecimal balance, String status) {}
    public record RemotePayment(
            UUID id, String operationId, Long sourceAccountId, Long targetAccountId,
            BigDecimal amount, String type, String status, Instant createdAt) {}
}
