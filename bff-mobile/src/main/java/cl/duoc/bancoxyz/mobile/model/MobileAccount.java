package cl.duoc.bancoxyz.mobile.model;

import java.math.BigDecimal;

public record MobileAccount(
        long accountId,
        String type,
        BigDecimal balance
) {
}
