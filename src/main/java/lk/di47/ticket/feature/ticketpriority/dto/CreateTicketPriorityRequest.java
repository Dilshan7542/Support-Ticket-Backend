package lk.di47.ticket.feature.ticketpriority.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketPriorityRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotBlank(message = "Priority name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Priority code is required")
        @Size(max = 64)
        String code,

        @Size(max = 255)
        String description
) {
}
