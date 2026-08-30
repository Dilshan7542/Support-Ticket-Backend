package lk.di47.ticket.feature.ticketstatus.service;

import lk.di47.ticket.feature.ticketstatus.dto.*;
import lk.di47.ticket.response.PageResponse;

public interface TicketStatusMasterService {
    TicketStatusMasterResponse create(CreateTicketStatusRequest request);

    PageResponse<TicketStatusMasterResponse> list(ListTicketStatusRequest request);

    TicketStatusMasterResponse detail(TicketStatusDetailRequest request);

    TicketStatusMasterResponse update(UpdateTicketStatusMasterRequest request);
}
