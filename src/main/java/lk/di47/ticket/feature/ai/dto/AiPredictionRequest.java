package lk.di47.ticket.feature.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record AiPredictionRequest(
        @NotBlank(message = "Subject is required")
        String subject,

        @NotBlank(message = "Description is required")
        String description
) {
}
