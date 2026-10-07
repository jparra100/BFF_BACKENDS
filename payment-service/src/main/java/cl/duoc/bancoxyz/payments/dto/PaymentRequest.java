package cl.duoc.bancoxyz.payments.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record PaymentRequest(@NotBlank String operationId,Long sourceAccountId,Long targetAccountId,@NotNull @DecimalMin("0.01") BigDecimal amount,@NotBlank String type){}
