package lk.di47.ticket.feature.ticket.dto;

import org.springframework.core.io.Resource;

public record TicketAttachmentDownload(
        Long id,
        Resource resource,
        String originalFileName,
        String contentType,
        long fileSize
) {
}
