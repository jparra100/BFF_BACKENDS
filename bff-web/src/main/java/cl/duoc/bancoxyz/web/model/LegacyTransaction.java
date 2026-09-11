package cl.duoc.bancoxyz.web.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LegacyTransaction(
        long id,
        LocalDate date,
        BigDecimal amount,
        String type
) {
}

