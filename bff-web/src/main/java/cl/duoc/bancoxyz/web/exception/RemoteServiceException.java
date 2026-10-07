package cl.duoc.bancoxyz.web.exception;

public class RemoteServiceException extends RuntimeException {
    public RemoteServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
