package lk.di47.ticket.feature.ai.service;

import lk.di47.ticket.feature.ai.dto.AiPredictionRequest;
import lk.di47.ticket.feature.ai.dto.AiPredictionResponse;

public interface AiPredictionService {
    AiPredictionResponse predictTicket(AiPredictionRequest request);
}
