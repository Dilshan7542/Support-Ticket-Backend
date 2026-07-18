package lk.di47.ticket.feature.ticket.dto;

import lk.di47.ticket.util.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TicketResponse(
        Long id,
        String ticketNo,
        Long customerId,
        Long companyId,
        String companyName,
        Long departmentId,
        String departmentName,
        Long categoryId,
        Long assignedStaffId,
        String assignedStaffName,
        String subject,
        String description,
        String categoryCode,
        String categoryName,
        String priority,
        TicketStatus status,
        LocalDateTime createdAt,
        List<TicketAttachmentSummary> attachments,
        List<TicketReplyResponse> replies,
        List<TicketTrackingResponse> tracking
) {
}
