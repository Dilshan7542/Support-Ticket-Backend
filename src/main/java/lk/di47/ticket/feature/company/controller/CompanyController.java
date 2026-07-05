package lk.di47.ticket.feature.company.controller;

import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.endpoint.CompanyEndpoint;
import lk.di47.ticket.feature.company.dto.*;
import lk.di47.ticket.feature.company.service.CompanyService;
import lk.di47.ticket.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping(CompanyEndpoint.CREATE)
    public ApiResponse<CompanyResponse> create(@Valid @RequestBody CreateCompanyRequest request) {
        return ApiResponse.success(MessageConstant.CREATED, companyService.create(request));
    }

    @PostMapping(CompanyEndpoint.LIST)
    public ApiResponse<List<CompanyResponse>> list(@Valid @RequestBody ListCompanyRequest request) {
        return ApiResponse.success(MessageConstant.SUCCESS, companyService.list());
    }

    @PostMapping(CompanyEndpoint.DETAIL)
    public ApiResponse<CompanyResponse> detail(@Valid @RequestBody CompanyDetailRequest request) {
        return ApiResponse.success(MessageConstant.SUCCESS, companyService.detail(request));
    }

    @PostMapping(CompanyEndpoint.UPDATE)
    public ApiResponse<CompanyResponse> update(@Valid @RequestBody UpdateCompanyRequest request) {
        return ApiResponse.success(MessageConstant.UPDATED, companyService.update(request));
    }
}
