package lk.di47.ticket.feature.department.dto;

import jakarta.validation.constraints.NotNull;

public record DepartmentDetailRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "departmentId is required")
        Long departmentId
) {
}
