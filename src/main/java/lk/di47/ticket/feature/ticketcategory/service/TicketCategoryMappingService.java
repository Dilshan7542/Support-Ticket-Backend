package lk.di47.ticket.feature.ticketcategory.service;

import lk.di47.ticket.feature.ticketcategory.dto.CreateTicketCategoryMappingRequest;
import lk.di47.ticket.feature.ticketcategory.dto.ListTicketCategoryMappingRequest;
import lk.di47.ticket.feature.ticketcategory.dto.TicketCategoryMappingDetailRequest;
import lk.di47.ticket.feature.ticketcategory.dto.TicketCategoryMappingResponse;
import lk.di47.ticket.feature.ticketcategory.dto.UpdateTicketCategoryMappingRequest;
import lk.di47.ticket.response.PageResponse;

public interface TicketCategoryMappingService {
    TicketCategoryMappingResponse create(CreateTicketCategoryMappingRequest request);

    PageResponse<TicketCategoryMappingResponse> list(ListTicketCategoryMappingRequest request);

    TicketCategoryMappingResponse detail(TicketCategoryMappingDetailRequest request);

    TicketCategoryMappingResponse update(UpdateTicketCategoryMappingRequest request);
}
