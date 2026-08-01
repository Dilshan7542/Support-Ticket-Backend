package lk.di47.ticket.feature.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.di47.ticket.util.enums.Status;

public record UpdateDepartmentRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "departmentId is required")
        Long departmentId,

        @NotBlank(message = "Department name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "Department code is required")
        @Size(max = 50)
        String code,

        @NotNull(message = "vendorId is required")
        Long vendorId,

        @Size(max = 500)
        String description,

        Status status
) {
}
