package lk.di47.ticket.feature.ticket.service;

import lk.di47.ticket.feature.ticket.dto.*;
import lk.di47.ticket.response.PageResponse;

public interface TicketService {
    TicketResponse createTicket(CreateTicketRequest request);

    PageResponse<TicketResponse> getTickets(ListTicketRequest request);

    TicketResponse getTicket(TicketDetailRequest request);

    TicketResponse updateStatus(UpdateTicketStatusRequest request);

    TicketResponse assignTicket(AssignTicketRequest request);

    Long addReply(AddTicketReplyRequest request);
}
