package lk.di47.ticket.feature.department.dto;

import lk.di47.ticket.util.enums.Status;
import lk.di47.ticket.util.enums.UserRole;

public record DepartmentUserResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String phone,
        Long vendorId,
        UserRole role,
        Status status
) {
}
