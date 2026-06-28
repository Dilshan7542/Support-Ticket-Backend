package lk.di47.ticket.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret = "change-this-secret-value-in-production-minimum-32-chars";
    private long accessTokenExpirationMinutes = 30;
    private long refreshTokenExpirationMinutes = 1440;
}
