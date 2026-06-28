package lk.di47.ticket.client.ai;

import lk.di47.ticket.feature.ai.dto.AiPredictionRequest;
import lk.di47.ticket.feature.ai.dto.AiPredictionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PythonAiPredictionClient implements AiPredictionGateway {
    private final RestClient.Builder restClientBuilder;
    private final AiPredictionClientProperties properties;

    @Override
    public AiPredictionResponse predict(AiPredictionRequest request) {
        return restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build()
                .post()
                .uri("/api/v1/ai/predict-ticket")
                .body(request)
                .retrieve()
                .body(AiPredictionResponse.class);
    }
}
