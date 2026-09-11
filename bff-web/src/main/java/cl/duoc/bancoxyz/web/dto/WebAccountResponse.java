package cl.duoc.bancoxyz.web.dto;

import java.math.BigDecimal;

public record WebAccountResponse(
        long cuentaId,
        String titular,
        int edadTitular,
        String tipoCuenta,
        BigDecimal saldo
) {
}

