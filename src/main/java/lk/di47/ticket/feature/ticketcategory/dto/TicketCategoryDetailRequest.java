package lk.di47.ticket.feature.ticketcategory.dto;

import jakarta.validation.constraints.NotNull;

public record TicketCategoryDetailRequest(
        @NotNull(message = "categoryId is required")
        Long categoryId
) {
}
