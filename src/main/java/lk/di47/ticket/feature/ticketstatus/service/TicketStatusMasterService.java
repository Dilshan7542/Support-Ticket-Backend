package lk.di47.ticket.feature.ticketstatus.service;

import lk.di47.ticket.feature.ticketstatus.dto.*;

import java.util.List;

public interface TicketStatusMasterService {
    TicketStatusMasterResponse create(CreateTicketStatusRequest request);

    List<TicketStatusMasterResponse> list();

    TicketStatusMasterResponse detail(TicketStatusDetailRequest request);

    TicketStatusMasterResponse update(UpdateTicketStatusMasterRequest request);
}
