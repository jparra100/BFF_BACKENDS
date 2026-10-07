package cl.duoc.bancoxyz.accounts.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountRequest(@NotNull Long customerId, @NotBlank String type,
                             @NotNull @DecimalMin("0.00") BigDecimal initialBalance) {
}
