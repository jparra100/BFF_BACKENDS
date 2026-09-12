package cl.duoc.bancoxyz.mobile.dto;

import java.math.BigDecimal;

public record MobileAccountResponse(
        long id,
        String tipo,
        BigDecimal saldo
) {
}
