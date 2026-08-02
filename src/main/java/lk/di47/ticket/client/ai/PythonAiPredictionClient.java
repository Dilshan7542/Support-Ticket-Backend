package lk.di47.ticket.client.ai;

import lk.di47.ticket.feature.ai.dto.AiPredictionRequest;
import lk.di47.ticket.feature.ai.dto.AiPredictionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "mode", havingValue = "python")
@RequiredArgsConstructor
@Log4j2
public class PythonAiPredictionClient implements AiPredictionGateway {
    private final AiPredictionClientProperties properties;
    private final ObjectMapper mapper;

    @Override
    public AiPredictionResponse predict(AiPredictionRequest request) {
        try {
            String json = mapper.writeValueAsString(request);
            URI uri = URI.create(properties.getBaseUrl() + "/predict");

            log.info("Calling Python AI prediction API: {} with {} chars", uri, json.length());
            log.debug("Python AI prediction request body: {}", json);

            HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .header("X-API-Key", "12345678")
                    .header("X-ID", "12345678")

                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("Python AI prediction API failed with status {} and body: {}",
                        response.statusCode(),
                        response.body());
                throw new RuntimeException("Python AI prediction API failed with status " + response.statusCode());
            }

            return mapper.readValue(response.body(), AiPredictionResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Python AI prediction API", e);
        }
    }
}
