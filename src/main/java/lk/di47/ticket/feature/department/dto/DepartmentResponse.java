package lk.di47.ticket.feature.department.dto;

import lk.di47.ticket.util.enums.Status;

public record DepartmentResponse(
        Long id,
        String name,
        String description,
        Status status
) {
}
