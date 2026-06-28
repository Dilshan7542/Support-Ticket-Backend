package lk.di47.ticket.client.ai;

import lk.di47.ticket.feature.ai.dto.AiPredictionRequest;
import lk.di47.ticket.feature.ai.dto.AiPredictionResponse;

public interface AiPredictionGateway {
    AiPredictionResponse predict(AiPredictionRequest request);
}
