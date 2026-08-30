package lk.di47.ticket.client.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.ai")
public class AiPredictionClientProperties {
    private String mode = "python";
    private String baseUrl = "http://localhost:8000";
    private double confidenceThreshold = 0.60;
}
