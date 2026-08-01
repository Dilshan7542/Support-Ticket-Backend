package lk.di47.ticket.feature.vendor.controller;

import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.endpoint.VendorEndpoint;
import lk.di47.ticket.feature.vendor.dto.*;
import lk.di47.ticket.feature.vendor.service.VendorService;
import lk.di47.ticket.response.ApiResponse;
import lk.di47.ticket.response.PageResponse;
import lk.di47.ticket.util.mask.SensitiveDataMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequiredArgsConstructor
@Log4j2
public class VendorController {
    private final VendorService VendorService;
    private final JsonMapper jsonMapper;

    @PostMapping(VendorEndpoint.CREATE)
    public ApiResponse<VendorResponse> create(@Valid @RequestBody CreateVendorRequest request) {
        log.debug("Create Vendor -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.CREATED, VendorService.create(request));
    }

    @PostMapping(VendorEndpoint.LIST)
    public ApiResponse<PageResponse<VendorResponse>> list(@Valid @RequestBody ListVendorRequest request) {
        log.debug("List Vendor -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, VendorService.list(request));
    }

    @PostMapping(VendorEndpoint.DETAIL)
    public ApiResponse<VendorResponse> detail(@Valid @RequestBody VendorDetailRequest request) {
        log.debug("Detail Vendor -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, VendorService.detail(request));
    }

    @PostMapping(VendorEndpoint.UPDATE)
    public ApiResponse<VendorResponse> update(@Valid @RequestBody UpdateVendorRequest request) {
        log.debug("Update Vendor -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.UPDATED, VendorService.update(request));
    }

    private String toJson(Object data) {
        return SensitiveDataMasker.mask(jsonMapper.writeValueAsString(data));
    }
}
