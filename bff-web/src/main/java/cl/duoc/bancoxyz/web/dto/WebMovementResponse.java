package cl.duoc.bancoxyz.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WebMovementResponse(
        LocalDate fecha,
        String tipo,
        BigDecimal monto,
        String descripcion
) {
}

