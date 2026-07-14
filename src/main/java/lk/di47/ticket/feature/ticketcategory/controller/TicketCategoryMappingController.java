package lk.di47.ticket.feature.ticketcategory.controller;

import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.endpoint.TicketCategoryMappingEndpoint;
import lk.di47.ticket.feature.ticketcategory.dto.CreateTicketCategoryMappingRequest;
import lk.di47.ticket.feature.ticketcategory.dto.ListTicketCategoryMappingRequest;
import lk.di47.ticket.feature.ticketcategory.dto.TicketCategoryMappingDetailRequest;
import lk.di47.ticket.feature.ticketcategory.dto.TicketCategoryMappingResponse;
import lk.di47.ticket.feature.ticketcategory.dto.UpdateTicketCategoryMappingRequest;
import lk.di47.ticket.feature.ticketcategory.service.TicketCategoryMappingService;
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
public class TicketCategoryMappingController {
    private final TicketCategoryMappingService mappingService;
    private final JsonMapper jsonMapper;

    @PostMapping(TicketCategoryMappingEndpoint.CREATE)
    public ApiResponse<TicketCategoryMappingResponse> create(@Valid @RequestBody CreateTicketCategoryMappingRequest request) {
        log.debug("Create Ticket Category Mapping -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.CREATED, mappingService.create(request));
    }

    @PostMapping(TicketCategoryMappingEndpoint.LIST)
    public ApiResponse<PageResponse<TicketCategoryMappingResponse>> list(@Valid @RequestBody ListTicketCategoryMappingRequest request) {
        log.debug("List Ticket Category Mapping -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, mappingService.list(request));
    }

    @PostMapping(TicketCategoryMappingEndpoint.DETAIL)
    public ApiResponse<TicketCategoryMappingResponse> detail(@Valid @RequestBody TicketCategoryMappingDetailRequest request) {
        log.debug("Detail Ticket Category Mapping -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, mappingService.detail(request));
    }

    @PostMapping(TicketCategoryMappingEndpoint.UPDATE)
    public ApiResponse<TicketCategoryMappingResponse> update(@Valid @RequestBody UpdateTicketCategoryMappingRequest request) {
        log.debug("Update Ticket Category Mapping -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.UPDATED, mappingService.update(request));
    }

    private String toJson(Object data) {
        return SensitiveDataMasker.mask(jsonMapper.writeValueAsString(data));
    }
}
