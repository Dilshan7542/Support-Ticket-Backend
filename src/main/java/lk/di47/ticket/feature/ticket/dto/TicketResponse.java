package lk.di47.ticket.feature.ticket.dto;

import lk.di47.ticket.util.enums.TicketPriority;
import lk.di47.ticket.util.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;

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
        LocalDateTime createdAt,
        List<TicketAttachmentSummary> attachments,
        List<TicketReplyResponse> replies,
        List<TicketTrackingResponse> tracking
) {
}
