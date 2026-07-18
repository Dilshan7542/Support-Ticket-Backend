package lk.di47.ticket.feature.ticketpriority.dto;

import jakarta.validation.constraints.NotNull;

public record DeleteTicketPriorityRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "priorityId is required")
        Long priorityId
) {
}
