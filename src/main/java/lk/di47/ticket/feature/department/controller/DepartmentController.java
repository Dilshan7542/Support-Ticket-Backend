package lk.di47.ticket.feature.department.controller;

import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.endpoint.DepartmentEndpoint;
import lk.di47.ticket.feature.department.dto.*;
import lk.di47.ticket.feature.department.service.DepartmentService;
import lk.di47.ticket.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;

    @PostMapping(DepartmentEndpoint.CREATE)
    public ApiResponse<DepartmentResponse> create(@Valid @RequestBody CreateDepartmentRequest request) {
        return ApiResponse.success(MessageConstant.CREATED, departmentService.create(request));
    }

    @PostMapping(DepartmentEndpoint.LIST)
    public ApiResponse<List<DepartmentResponse>> list(@Valid @RequestBody ListDepartmentRequest request) {
        return ApiResponse.success(MessageConstant.SUCCESS, departmentService.list());
    }

    @PostMapping(DepartmentEndpoint.DETAIL)
    public ApiResponse<DepartmentResponse> detail(@Valid @RequestBody DepartmentDetailRequest request) {
        return ApiResponse.success(MessageConstant.SUCCESS, departmentService.detail(request));
    }

    @PostMapping(DepartmentEndpoint.UPDATE)
    public ApiResponse<DepartmentResponse> update(@Valid @RequestBody UpdateDepartmentRequest request) {
        return ApiResponse.success(MessageConstant.UPDATED, departmentService.update(request));
    }
}
