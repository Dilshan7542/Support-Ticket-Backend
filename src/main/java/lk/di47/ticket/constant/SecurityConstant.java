package lk.di47.ticket.constant;

public final class SecurityConstant {
    private SecurityConstant() {
    }

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String KEY_ID_HEADER = "X-Key-Id";
    public static final String TIMESTAMP_HEADER = "X-Timestamp";
    public static final String NONCE_HEADER = "X-Nonce";
    public static final String ENCRYPTION_HEADER = "X-Content-Encryption";
    public static final String CURRENT_USER_ID = "CURRENT_USER_ID";
    public static final String CURRENT_SESSION_ID = "CURRENT_SESSION_ID";
    public static final String CURRENT_ENCRYPTION_KEY_ID = "CURRENT_ENCRYPTION_KEY_ID";
}
