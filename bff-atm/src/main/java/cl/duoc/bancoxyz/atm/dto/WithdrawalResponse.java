package cl.duoc.bancoxyz.atm.dto;

import java.math.BigDecimal;

public record WithdrawalResponse(
        long cuentaId,
        BigDecimal montoRetirado,
        BigDecimal saldoDisponible
) {
}
