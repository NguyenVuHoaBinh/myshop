package viettel.telecom.backend.exception;

public class YamlSerializationException extends RuntimeException {
    public YamlSerializationException(String message) {
        super(message);
    }

    public YamlSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}