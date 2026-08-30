package lk.di47.ticket.feature.ai.service.impl;

import lk.di47.ticket.client.ai.AiPredictionGateway;
import lk.di47.ticket.feature.ai.dto.AiPredictionRequest;
import lk.di47.ticket.feature.ai.dto.AiPredictionResponse;
import lk.di47.ticket.feature.ai.service.AiPredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiPredictionServiceImpl implements AiPredictionService {
    private final AiPredictionGateway aiPredictionGateway;

    @Override
    public AiPredictionResponse predictTicket(AiPredictionRequest request) {
        return aiPredictionGateway.predict(request);
    }
}
