package lk.di47.ticket.feature.ticket.service;

import lk.di47.ticket.feature.ticket.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TicketService {
    TicketResponse createTicket(CreateTicketRequest request);

    List<TicketResponse> getTickets();

    TicketResponse getTicket(TicketDetailRequest request);

    TicketResponse updateStatus(UpdateTicketStatusRequest request);

    TicketResponse assignTicket(AssignTicketRequest request);

    Long addReply(AddTicketReplyRequest request);

    TicketAttachmentResponse uploadAttachment(Long userId, Long ticketId, MultipartFile file);
}
