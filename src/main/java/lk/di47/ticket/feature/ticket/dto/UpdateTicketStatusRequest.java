package lk.di47.ticket.feature.ticket.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.di47.ticket.util.enums.TicketStatus;

public record UpdateTicketStatusRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "ticketId is required")
        Long ticketId,

        @NotNull(message = "Status is required")
        TicketStatus status,

        @Size(max = 255)
        String remark
) {
}
