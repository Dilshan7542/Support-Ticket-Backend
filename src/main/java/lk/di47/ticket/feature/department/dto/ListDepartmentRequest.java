package lk.di47.ticket.feature.department.dto;

import jakarta.validation.constraints.NotNull;

public record ListDepartmentRequest(
        @NotNull(message = "userId is required")
        Long userId,

        Long companyId,

        Integer page,
        Integer size
) {
}
