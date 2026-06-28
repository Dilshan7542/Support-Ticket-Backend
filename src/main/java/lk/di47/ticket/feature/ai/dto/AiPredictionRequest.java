package lk.di47.ticket.feature.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AiPredictionRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotBlank(message = "Description is required")
        String description
) {
}
