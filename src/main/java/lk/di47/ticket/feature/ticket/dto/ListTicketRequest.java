package lk.di47.ticket.feature.ticket.dto;

import jakarta.validation.constraints.NotNull;

public record ListTicketRequest(
        @NotNull(message = "userId is required")
        Long userId
) {
}
