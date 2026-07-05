package lk.di47.ticket.feature.company.controller;

import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.endpoint.CompanyEndpoint;
import lk.di47.ticket.feature.company.dto.*;
import lk.di47.ticket.feature.company.service.CompanyService;
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
public class CompanyController {
    private final CompanyService companyService;
    private final JsonMapper jsonMapper;

    @PostMapping(CompanyEndpoint.CREATE)
    public ApiResponse<CompanyResponse> create(@Valid @RequestBody CreateCompanyRequest request) {
        log.debug("Create Company -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.CREATED, companyService.create(request));
    }

    @PostMapping(CompanyEndpoint.LIST)
    public ApiResponse<List<CompanyResponse>> list(@Valid @RequestBody ListCompanyRequest request) {
        log.debug("List Company -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, companyService.list());
    }

    @PostMapping(CompanyEndpoint.DETAIL)
    public ApiResponse<CompanyResponse> detail(@Valid @RequestBody CompanyDetailRequest request) {
        log.debug("Detail Company -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, companyService.detail(request));
    }

    @PostMapping(CompanyEndpoint.UPDATE)
    public ApiResponse<CompanyResponse> update(@Valid @RequestBody UpdateCompanyRequest request) {
        log.debug("Update Company -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.UPDATED, companyService.update(request));
    }

    private String toJson(Object data) {
        return SensitiveDataMasker.mask(jsonMapper.writeValueAsString(data));
    }
}
