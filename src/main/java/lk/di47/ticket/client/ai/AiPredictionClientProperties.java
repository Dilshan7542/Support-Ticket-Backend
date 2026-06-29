package lk.di47.ticket.client.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.ai")
public class AiPredictionClientProperties {
    private String mode = "java";
    private String baseUrl = "http://localhost:5000";
}
