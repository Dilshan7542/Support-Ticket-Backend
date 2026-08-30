package lk.di47.ticket.feature.ticketcategory.dto;

import jakarta.validation.constraints.NotNull;

public record TicketCategoryMappingDetailRequest(
        @NotNull(message = "mappingId is required")
        Long mappingId
) {
}
