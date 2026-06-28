package lk.di47.ticket.feature.department.service;

import lk.di47.ticket.feature.department.dto.CreateDepartmentRequest;
import lk.di47.ticket.feature.department.dto.DepartmentDetailRequest;
import lk.di47.ticket.feature.department.dto.DepartmentResponse;
import lk.di47.ticket.feature.department.dto.UpdateDepartmentRequest;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse create(CreateDepartmentRequest request);

    List<DepartmentResponse> list();

    DepartmentResponse detail(DepartmentDetailRequest request);

    DepartmentResponse update(UpdateDepartmentRequest request);
}
