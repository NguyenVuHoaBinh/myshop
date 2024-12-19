package viettel.telecom.backend.exception;

public class InvalidDatabaseConfigException extends RuntimeException {
    public InvalidDatabaseConfigException(String message) {
        super(message);
    }

    public InvalidDatabaseConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
