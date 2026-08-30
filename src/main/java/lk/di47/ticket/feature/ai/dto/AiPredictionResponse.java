package lk.di47.ticket.feature.ai.dto;

public record AiPredictionResponse(
        String category,
        Double categoryConfidence,
        String priority,
        Double priorityConfidence,
        Boolean requiresManualReview
) {
}
