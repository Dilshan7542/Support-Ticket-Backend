package lk.di47.ticket.feature.ticketcategory.dto;

public record ListTicketCategoryRequest(
        Long companyId,
        Long departmentId,

        Integer page,
        Integer size
) {
}
