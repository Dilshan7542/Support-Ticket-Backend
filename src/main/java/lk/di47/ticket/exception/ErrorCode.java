package lk.di47.ticket.exception;

public enum ErrorCode {
    INVALID_REQUEST(true, "Request failed"),
    NOT_FOUND(true, "Requested resource was not found"),
    UNAUTHORIZED(false, "Authentication failed"),
    CRYPTO_INVALID_REQUEST(false, "Invalid encrypted request"),
    KEY_EXCHANGE_FAILED(false, "Unable to complete key exchange"),
    ENCRYPTION_SESSION_EXPIRED(false, "Encryption session expired"),
    FORBIDDEN(false, "Access denied"),
    INTERNAL_ERROR(false, "Internal server error");

    private final boolean display;
    private final String defaultMessage;

    ErrorCode(boolean display, String defaultMessage) {
        this.display = display;
        this.defaultMessage = defaultMessage;
    }

    public boolean isDisplay() {
        return display;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
