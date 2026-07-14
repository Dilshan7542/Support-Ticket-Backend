package lk.di47.ticket.feature.ticketcategory.dto;

import lk.di47.ticket.util.enums.Status;

public record TicketCategoryMappingResponse(
        Long id,
        Long companyId,
        String companyName,
        Long categoryId,
        String categoryCode,
        String categoryName,
        Long departmentId,
        String departmentName,
        Status status
) {
}
