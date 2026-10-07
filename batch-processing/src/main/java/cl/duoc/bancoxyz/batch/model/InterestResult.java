package cl.duoc.bancoxyz.batch.model;

import java.math.BigDecimal;

public record InterestResult(long accountId, String holderName, String accountType,
                             BigDecimal balance, BigDecimal monthlyInterest) {
}
