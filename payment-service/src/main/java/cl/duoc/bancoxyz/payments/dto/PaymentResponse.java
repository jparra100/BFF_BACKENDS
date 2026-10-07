package cl.duoc.bancoxyz.payments.dto;
import java.math.BigDecimal; import java.time.Instant; import java.util.UUID;
public record PaymentResponse(UUID id,String operationId,Long sourceAccountId,Long targetAccountId,BigDecimal amount,String type,String status,Instant createdAt){}
