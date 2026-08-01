package lk.di47.ticket.feature.ticket.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssignTicketRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "ticketId is required")
        Long ticketId,

        @NotNull(message = "departmentId is required")
        Long departmentId,

        @Size(max = 64)
        String categoryCode,

        @Size(max = 64)
        String priority,

        Long assignedStaffId
) {
}
