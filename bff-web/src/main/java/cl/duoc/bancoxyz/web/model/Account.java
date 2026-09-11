package cl.duoc.bancoxyz.web.model;

import java.math.BigDecimal;

public record Account(
        long accountId,
        String holderName,
        BigDecimal balance,
        int holderAge,
        String accountType
) {
}

