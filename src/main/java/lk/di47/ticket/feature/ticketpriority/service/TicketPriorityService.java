package lk.di47.ticket.feature.ticketpriority.service;

import lk.di47.ticket.feature.ticketpriority.dto.*;
import lk.di47.ticket.response.PageResponse;

public interface TicketPriorityService {
    TicketPriorityResponse create(CreateTicketPriorityRequest request);

    PageResponse<TicketPriorityResponse> list(ListTicketPriorityRequest request);

    TicketPriorityResponse detail(TicketPriorityDetailRequest request);

    TicketPriorityResponse update(UpdateTicketPriorityRequest request);

    TicketPriorityResponse delete(DeleteTicketPriorityRequest request);
}
