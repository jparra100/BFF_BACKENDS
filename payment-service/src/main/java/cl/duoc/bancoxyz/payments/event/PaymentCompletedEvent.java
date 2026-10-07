package cl.duoc.bancoxyz.payments.event;
import java.math.BigDecimal; import java.time.Instant; import java.util.UUID;
public record PaymentCompletedEvent(UUID eventId,String operationId,Long sourceAccountId,Long targetAccountId,BigDecimal amount,String type,Instant occurredAt){}
