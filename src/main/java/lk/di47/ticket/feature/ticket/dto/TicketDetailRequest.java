package lk.di47.ticket.feature.ticket.dto;

import jakarta.validation.constraints.NotNull;

public record TicketDetailRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "ticketId is required")
        Long ticketId
) {
}
