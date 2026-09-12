package cl.duoc.bancoxyz.mobile.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MobileMovement(
        long accountId,
        LocalDate date,
        String type,
        BigDecimal amount
) {
}
