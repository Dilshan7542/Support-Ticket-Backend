package lk.di47.ticket.feature.department.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record DepartmentUsersRequest(
        @NotNull(message = "userId is required")
        Long userId,

        Long departmentId,

        Long categoryId,

        String categoryCode
) {
    @AssertTrue(message = "departmentId, categoryId or categoryCode is required")
    public boolean hasDepartmentOrCategory() {
        return departmentId != null
                || categoryId != null
                || (categoryCode != null && !categoryCode.isBlank());
    }
}
