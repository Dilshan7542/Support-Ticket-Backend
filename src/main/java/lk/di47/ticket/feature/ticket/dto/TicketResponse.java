package lk.di47.ticket.feature.ticket.dto;

import lk.di47.ticket.util.enums.TicketPriority;
import lk.di47.ticket.util.enums.TicketStatus;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String ticketNo,
        Long customerId,
        Long departmentId,
        Long assignedStaffId,
        String subject,
        String description,
        String category,
        TicketPriority priority,
        TicketStatus status,
        LocalDateTime createdAt
) {
}
