package cl.duoc.bancoxyz.mobile.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MobileMovementResponse(
        LocalDate fecha,
        String tipo,
        BigDecimal monto
) {
}
