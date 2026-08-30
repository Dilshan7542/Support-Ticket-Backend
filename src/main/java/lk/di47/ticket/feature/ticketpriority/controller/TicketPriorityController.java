package lk.di47.ticket.feature.ticketpriority.controller;

import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.endpoint.TicketPriorityEndpoint;
import lk.di47.ticket.feature.ticketpriority.dto.*;
import lk.di47.ticket.feature.ticketpriority.service.TicketPriorityService;
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
public class TicketPriorityController {
    private final TicketPriorityService ticketPriorityService;
    private final JsonMapper jsonMapper;

    @PostMapping(TicketPriorityEndpoint.CREATE)
    public ApiResponse<TicketPriorityResponse> create(@Valid @RequestBody CreateTicketPriorityRequest request) {
        log.debug("Create Ticket Priority -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.CREATED, ticketPriorityService.create(request));
    }

    @PostMapping(TicketPriorityEndpoint.LIST)
    public ApiResponse<PageResponse<TicketPriorityResponse>> list(@Valid @RequestBody ListTicketPriorityRequest request) {
        log.debug("List Ticket Priority -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, ticketPriorityService.list(request));
    }

    @PostMapping(TicketPriorityEndpoint.DETAIL)
    public ApiResponse<TicketPriorityResponse> detail(@Valid @RequestBody TicketPriorityDetailRequest request) {
        log.debug("Detail Ticket Priority -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, ticketPriorityService.detail(request));
    }

    @PostMapping(TicketPriorityEndpoint.UPDATE)
    public ApiResponse<TicketPriorityResponse> update(@Valid @RequestBody UpdateTicketPriorityRequest request) {
        log.debug("Update Ticket Priority -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.UPDATED, ticketPriorityService.update(request));
    }

    @PostMapping(TicketPriorityEndpoint.DELETE)
    public ApiResponse<TicketPriorityResponse> delete(@Valid @RequestBody DeleteTicketPriorityRequest request) {
        log.debug("Delete Ticket Priority -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.UPDATED, ticketPriorityService.delete(request));
    }

    private String toJson(Object data) {
        return SensitiveDataMasker.mask(jsonMapper.writeValueAsString(data));
    }
}
