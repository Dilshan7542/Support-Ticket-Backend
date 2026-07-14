package lk.di47.ticket.feature.ticketcategory.dto;

import lk.di47.ticket.util.enums.Status;

public record TicketCategoryResponse(
        Long id,
        String name,
        String code,
        String description,
        Status status
) {
}
