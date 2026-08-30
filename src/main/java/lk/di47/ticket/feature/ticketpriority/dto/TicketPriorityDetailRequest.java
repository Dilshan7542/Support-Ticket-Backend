package lk.di47.ticket.feature.ticketpriority.dto;

import jakarta.validation.constraints.NotNull;

public record TicketPriorityDetailRequest(
        @NotNull(message = "priorityId is required")
        Long priorityId
) {
}
