package lk.di47.ticket.client.ai;

import lk.di47.ticket.feature.ai.dto.AiPredictionRequest;
import lk.di47.ticket.feature.ai.dto.AiPredictionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "mode", havingValue = "python")
@RequiredArgsConstructor
@Log4j2
public class PythonAiPredictionClient implements AiPredictionGateway {
    private final RestClient.Builder restClientBuilder;
    private final AiPredictionClientProperties properties;
    private final ObjectMapper mapper;

    @Override
    public AiPredictionResponse predict(AiPredictionRequest request) {
        try {
            String json = mapper.writeValueAsString(request);
            byte[] requestBody = json.getBytes(StandardCharsets.UTF_8);

            log.info("Calling Python AI prediction API: {}/predict with {} bytes",
                    properties.getBaseUrl(),
                    requestBody.length);
            log.debug("Python AI prediction request body: {}", json);

            return restClientBuilder
                    .baseUrl(properties.getBaseUrl())
                    .build()
                    .post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("X-API-Key", "12345678")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(requestBody.length))
                    .body(requestBody)
                    .retrieve()
                    .body(AiPredictionResponse.class);

        } catch (RestClientResponseException exception) {
            log.error("Python AI prediction API failed with status {} and body: {}",
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString(),
                    exception);
            throw new RuntimeException("Python AI prediction API failed", exception);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Python AI prediction API", e);
        }
    }
}
