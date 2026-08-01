package lk.di47.ticket.feature.ticket.dto;

import lk.di47.ticket.util.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TicketResponse(
        Long id,
        String ticketNo,
        Long customerId,
        Long vendorId,
        String vendorName,
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
        Double categoryConfidence,
        Double priorityConfidence,
        Boolean requiresManualReview,
        TicketStatus status,
        LocalDateTime createdAt,
        List<TicketAttachmentSummary> attachments,
        List<TicketReplyResponse> replies,
        List<TicketTrackingResponse> tracking
) {
}
