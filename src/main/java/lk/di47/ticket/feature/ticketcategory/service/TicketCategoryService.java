package lk.di47.ticket.feature.ticketcategory.service;

import lk.di47.ticket.feature.ticketcategory.dto.*;
import lk.di47.ticket.response.PageResponse;

public interface TicketCategoryService {
    TicketCategoryResponse create(CreateTicketCategoryRequest request);

    PageResponse<TicketCategoryResponse> list(ListTicketCategoryRequest request);

    TicketCategoryResponse detail(TicketCategoryDetailRequest request);

    TicketCategoryResponse update(UpdateTicketCategoryRequest request);
}
