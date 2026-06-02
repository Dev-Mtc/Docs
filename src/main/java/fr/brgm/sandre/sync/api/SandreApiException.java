package fr.brgm.sandre.sync.api;

public class SandreApiException extends RuntimeException {

    public SandreApiException(String message) {
        super(message);
    }

    public SandreApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
