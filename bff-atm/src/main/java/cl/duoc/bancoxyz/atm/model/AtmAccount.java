package cl.duoc.bancoxyz.atm.model;

import java.math.BigDecimal;
import java.util.Optional;

public class AtmAccount {

    private final long accountId;
    private BigDecimal balance;

    public AtmAccount(long accountId, BigDecimal balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public long accountId() {
        return accountId;
    }

    public synchronized BigDecimal balance() {
        return balance;
    }

    public synchronized Optional<BigDecimal> withdraw(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            return Optional.empty();
        }
        balance = balance.subtract(amount);
        return Optional.of(balance);
    }
}
