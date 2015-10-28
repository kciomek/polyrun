package polyrun.exceptions;

public class UnboundedSystemException extends Exception {
    public UnboundedSystemException(String message) {
        super(message);
    }

    public UnboundedSystemException(Exception e) {
        super(e);
    }

    public UnboundedSystemException(String message, Exception e) {
        super(message, e);
    }

    public UnboundedSystemException() {
    }
}
