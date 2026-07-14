package lk.di47.ticket.feature.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDepartmentRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotBlank(message = "Department name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Department code is required")
        @Size(max = 30)
        String code,

        @NotNull(message = "companyId is required")
        Long companyId,

        @Size(max = 255)
        String description
) {
}
