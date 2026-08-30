package lk.di47.ticket.feature.ticketstatus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.di47.ticket.util.enums.Status;

public record UpdateTicketStatusMasterRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "statusId is required")
        Long statusId,

        @NotBlank(message = "Status name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Status code is required")
        @Size(max = 64)
        String code,

        @Size(max = 255)
        String description,

        Status status
) {
}
