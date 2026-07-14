package lk.di47.ticket.feature.ticketcategory.dto;

import jakarta.validation.constraints.NotNull;

public record CreateTicketCategoryMappingRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "companyId is required")
        Long companyId,

        @NotNull(message = "categoryId is required")
        Long categoryId,

        @NotNull(message = "departmentId is required")
        Long departmentId
) {
}
