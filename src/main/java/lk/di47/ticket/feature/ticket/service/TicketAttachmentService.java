package lk.di47.ticket.feature.ticket.service;

import lk.di47.ticket.feature.ticket.dto.TicketAttachmentDownload;
import lk.di47.ticket.feature.ticket.dto.TicketAttachmentResponse;
import org.springframework.web.multipart.MultipartFile;

public interface TicketAttachmentService {
    TicketAttachmentResponse uploadAttachment(Long userId, Long ticketId, MultipartFile file);

    TicketAttachmentDownload downloadAttachment(Long attachmentId, Long requesterUserId);
}
