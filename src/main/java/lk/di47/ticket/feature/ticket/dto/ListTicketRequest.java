package lk.di47.ticket.feature.ticket.dto;

import jakarta.validation.constraints.NotNull;
import lk.di47.ticket.util.enums.TicketStatus;

public record ListTicketRequest(
        @NotNull(message = "userId is required")
        Long userId,

        Long ticketId,
        String ticketNo,
        Long customerId,
        String customerName,
        Long companyId,
        String companyName,
        Long departmentId,
        String departmentName,
        Long assignedStaffId,
        String assignedStaffName,
        Long categoryId,
        String categoryCode,
        String categoryName,
        String subject,
        String priority,
        TicketStatus status,

        Integer page,
        Integer size
) {
}
