package lk.di47.ticket.crypto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.crypto")
public class CryptoProperties {
    private boolean enabled = true;
    private int sessionTtlMinutes = 30;
    private int timestampToleranceSeconds = 120;
    private String secretKey = "12345678901234567890123456789012";
    private List<String> excludedPaths = new ArrayList<>();
}
