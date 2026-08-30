package lk.di47.ticket.feature.ticketcategory.dto;

public record ListTicketCategoryMappingRequest(
        Long categoryId,
        Integer page,
        Integer size
) {
}
