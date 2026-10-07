package cl.duoc.bancoxyz.customers.event;
import java.math.BigDecimal; import java.time.Instant; import java.util.UUID;
public record SecurityAlertEvent(UUID eventId,String operationId,Long customerId,BigDecimal amount,String reason,Instant occurredAt){}
