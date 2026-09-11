package cl.duoc.bancoxyz.web.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AnnualMovement(
        long accountId,
        LocalDate date,
        String type,
        BigDecimal amount,
        String description
) {
}

