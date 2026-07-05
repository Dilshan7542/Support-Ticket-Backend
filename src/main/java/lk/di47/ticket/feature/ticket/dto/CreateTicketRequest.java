package lk.di47.ticket.feature.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotBlank(message = "Subject is required")
        @Size(max = 150)
        String subject,

        @NotBlank(message = "Description is required")
        @Size(max = 2000)
        String description,

        @Size(max = 64)
        String categoryCode
) {
}
