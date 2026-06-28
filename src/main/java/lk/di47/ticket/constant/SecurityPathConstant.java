package lk.di47.ticket.constant;

public final class SecurityPathConstant {
    private SecurityPathConstant() {
    }

    public static final String[] PUBLIC_PATHS = {
            "/api/v1/security/key-exchange",
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token",
            "/api/v1/health/status",
            "/h2-console/**",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    public static final String[] LOGGING_EXCLUDED_PATHS = {
            "/h2-console/**",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };
}
