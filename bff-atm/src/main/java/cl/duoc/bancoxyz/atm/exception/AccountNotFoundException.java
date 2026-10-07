package cl.duoc.bancoxyz.atm.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException() {
        super("La cuenta solicitada no existe");
    }

    public AccountNotFoundException(long accountId) {
        super("No existe la cuenta " + accountId);
    }
}
