package lk.di47.ticket.feature.ticketcategory.controller;

import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.endpoint.TicketCategoryEndpoint;
import lk.di47.ticket.feature.ticketcategory.dto.*;
import lk.di47.ticket.feature.ticketcategory.service.TicketCategoryService;
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
public class TicketCategoryController {
    private final TicketCategoryService ticketCategoryService;
    private final JsonMapper jsonMapper;

    @PostMapping(TicketCategoryEndpoint.CREATE)
    public ApiResponse<TicketCategoryResponse> create(@Valid @RequestBody CreateTicketCategoryRequest request) {
        log.debug("Create Ticket Category -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.CREATED, ticketCategoryService.create(request));
    }

    @PostMapping(TicketCategoryEndpoint.LIST)
    public ApiResponse<List<TicketCategoryResponse>> list(@Valid @RequestBody ListTicketCategoryRequest request) {
        log.debug("List Ticket Category -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, ticketCategoryService.list());
    }

    @PostMapping(TicketCategoryEndpoint.DETAIL)
    public ApiResponse<TicketCategoryResponse> detail(@Valid @RequestBody TicketCategoryDetailRequest request) {
        log.debug("Detail Ticket Category -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, ticketCategoryService.detail(request));
    }

    @PostMapping(TicketCategoryEndpoint.UPDATE)
    public ApiResponse<TicketCategoryResponse> update(@Valid @RequestBody UpdateTicketCategoryRequest request) {
        log.debug("Update Ticket Category -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.UPDATED, ticketCategoryService.update(request));
    }

    private String toJson(Object data) {
        return SensitiveDataMasker.mask(jsonMapper.writeValueAsString(data));
    }
}
