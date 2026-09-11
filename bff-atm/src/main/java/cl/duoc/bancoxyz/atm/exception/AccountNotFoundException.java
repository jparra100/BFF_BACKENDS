package cl.duoc.bancoxyz.atm.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(long accountId) {
        super("No existe la cuenta " + accountId);
    }
}
