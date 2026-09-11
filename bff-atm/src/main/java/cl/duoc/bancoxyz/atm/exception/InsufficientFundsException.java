package cl.duoc.bancoxyz.atm.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException() {
        super("Saldo insuficiente para realizar el retiro");
    }
}
