package lk.di47.ticket.client.ai;

import lk.di47.ticket.feature.ai.dto.AiPredictionRequest;
import lk.di47.ticket.feature.ai.dto.AiPredictionResponse;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "mode", havingValue = "java")
public class JavaAiPredictionClient implements AiPredictionGateway {

    @Override
    public AiPredictionResponse predict(AiPredictionRequest request) {
        throw new BusinessException(ErrorCode.INVALID_REQUEST, "Java AI prediction is not implemented for this backend");
    }
}
