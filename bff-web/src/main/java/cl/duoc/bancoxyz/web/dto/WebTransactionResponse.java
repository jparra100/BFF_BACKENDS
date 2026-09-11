package cl.duoc.bancoxyz.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WebTransactionResponse(
        long id,
        LocalDate fecha,
        String tipo,
        BigDecimal monto
) {
}

