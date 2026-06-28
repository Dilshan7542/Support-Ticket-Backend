package lk.di47.ticket.feature.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddTicketReplyRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "ticketId is required")
        Long ticketId,

        @NotBlank(message = "Message is required")
        @Size(max = 2000)
        String message
) {
}
