package lk.di47.ticket.feature.ticketstatus.dto;

import jakarta.validation.constraints.NotNull;

public record TicketStatusDetailRequest(
        @NotNull(message = "statusId is required")
        Long statusId
) {
}
