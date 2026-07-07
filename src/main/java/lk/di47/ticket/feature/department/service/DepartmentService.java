package lk.di47.ticket.feature.department.service;

import lk.di47.ticket.feature.department.dto.CreateDepartmentRequest;
import lk.di47.ticket.feature.department.dto.DepartmentDetailRequest;
import lk.di47.ticket.feature.department.dto.DepartmentResponse;
import lk.di47.ticket.feature.department.dto.ListDepartmentRequest;
import lk.di47.ticket.feature.department.dto.UpdateDepartmentRequest;
import lk.di47.ticket.response.PageResponse;

public interface DepartmentService {
    DepartmentResponse create(CreateDepartmentRequest request);

    PageResponse<DepartmentResponse> list(ListDepartmentRequest request);

    DepartmentResponse detail(DepartmentDetailRequest request);

    DepartmentResponse update(UpdateDepartmentRequest request);
}
