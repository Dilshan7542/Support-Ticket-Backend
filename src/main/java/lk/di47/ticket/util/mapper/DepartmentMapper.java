package lk.di47.ticket.util.mapper;

import lk.di47.ticket.entity.Department;
import lk.di47.ticket.feature.department.dto.DepartmentResponse;

public final class DepartmentMapper {
    private DepartmentMapper() {
    }

    public static DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getStatus()
        );
    }
}
