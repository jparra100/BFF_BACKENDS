package cl.duoc.bancoxyz.atm.exception;

public class RemoteServiceException extends RuntimeException {
    public RemoteServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
