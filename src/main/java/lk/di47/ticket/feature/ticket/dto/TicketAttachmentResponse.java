package lk.di47.ticket.feature.ticket.dto;

import java.time.LocalDateTime;

public record TicketAttachmentResponse(
        Long id,
        Long ticketId,
        Long uploadedByUserId,
        String originalFileName,
        String storedFileName,
        String contentType,
        Long fileSize,
        String storagePath,
        LocalDateTime createdAt
) {
}
