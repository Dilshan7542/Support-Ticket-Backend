package lk.di47.ticket.feature.ticketcategory.service;

import lk.di47.ticket.feature.ticketcategory.dto.*;

import java.util.List;

public interface TicketCategoryService {
    TicketCategoryResponse create(CreateTicketCategoryRequest request);

    List<TicketCategoryResponse> list();

    TicketCategoryResponse detail(TicketCategoryDetailRequest request);

    TicketCategoryResponse update(UpdateTicketCategoryRequest request);
}
