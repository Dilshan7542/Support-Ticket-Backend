package lk.di47.ticket.feature.ai.dto;

public record AiPredictionResponse(
        String category,
        String priority,
        Long suggestedDepartmentId,
        Double confidenceScore
) {
}
