package lk.di47.ticket.feature.ticketcategory.dto;

import jakarta.validation.constraints.NotNull;
import lk.di47.ticket.util.enums.Status;

public record UpdateTicketCategoryMappingRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "mappingId is required")
        Long mappingId,

        @NotNull(message = "companyId is required")
        Long companyId,

        @NotNull(message = "categoryId is required")
        Long categoryId,

        @NotNull(message = "departmentId is required")
        Long departmentId,

        Status status
) {
}
