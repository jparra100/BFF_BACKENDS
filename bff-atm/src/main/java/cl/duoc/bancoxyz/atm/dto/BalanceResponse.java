package cl.duoc.bancoxyz.atm.dto;

import java.math.BigDecimal;

public record BalanceResponse(long cuentaId, BigDecimal saldoDisponible) {
}
