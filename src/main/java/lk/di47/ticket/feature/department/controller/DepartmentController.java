package lk.di47.ticket.feature.department.controller;

import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.endpoint.DepartmentEndpoint;
import lk.di47.ticket.feature.department.dto.*;
import lk.di47.ticket.feature.department.service.DepartmentService;
import lk.di47.ticket.response.ApiResponse;
import lk.di47.ticket.util.mask.SensitiveDataMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
public class DepartmentController {
    private final DepartmentService departmentService;
    private final JsonMapper jsonMapper;

    @PostMapping(DepartmentEndpoint.CREATE)
    public ApiResponse<DepartmentResponse> create(@Valid @RequestBody CreateDepartmentRequest request) {
        log.debug("Create Department -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.CREATED, departmentService.create(request));
    }

    @PostMapping(DepartmentEndpoint.LIST)
    public ApiResponse<List<DepartmentResponse>> list(@Valid @RequestBody ListDepartmentRequest request) {
        log.debug("List Department -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, departmentService.list());
    }

    @PostMapping(DepartmentEndpoint.DETAIL)
    public ApiResponse<DepartmentResponse> detail(@Valid @RequestBody DepartmentDetailRequest request) {
        log.debug("Detail Department -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, departmentService.detail(request));
    }

    @PostMapping(DepartmentEndpoint.UPDATE)
    public ApiResponse<DepartmentResponse> update(@Valid @RequestBody UpdateDepartmentRequest request) {
        log.debug("Update Department -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.UPDATED, departmentService.update(request));
    }

    private String toJson(Object data) {
        return SensitiveDataMasker.mask(jsonMapper.writeValueAsString(data));
    }
}
