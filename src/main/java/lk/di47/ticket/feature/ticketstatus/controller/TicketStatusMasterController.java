package lk.di47.ticket.feature.ticketstatus.controller;

import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.endpoint.TicketStatusEndpoint;
import lk.di47.ticket.feature.ticketstatus.dto.*;
import lk.di47.ticket.feature.ticketstatus.service.TicketStatusMasterService;
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
public class TicketStatusMasterController {
    private final TicketStatusMasterService ticketStatusMasterService;
    private final JsonMapper jsonMapper;

    @PostMapping(TicketStatusEndpoint.CREATE)
    public ApiResponse<TicketStatusMasterResponse> create(@Valid @RequestBody CreateTicketStatusRequest request) {
        log.debug("Create Ticket Status -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.CREATED, ticketStatusMasterService.create(request));
    }

    @PostMapping(TicketStatusEndpoint.LIST)
    public ApiResponse<List<TicketStatusMasterResponse>> list(@Valid @RequestBody ListTicketStatusRequest request) {
        log.debug("List Ticket Status -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, ticketStatusMasterService.list());
    }

    @PostMapping(TicketStatusEndpoint.DETAIL)
    public ApiResponse<TicketStatusMasterResponse> detail(@Valid @RequestBody TicketStatusDetailRequest request) {
        log.debug("Detail Ticket Status -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, ticketStatusMasterService.detail(request));
    }

    @PostMapping(TicketStatusEndpoint.UPDATE)
    public ApiResponse<TicketStatusMasterResponse> update(@Valid @RequestBody UpdateTicketStatusMasterRequest request) {
        log.debug("Update Ticket Status -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.UPDATED, ticketStatusMasterService.update(request));
    }

    private String toJson(Object data) {
        return SensitiveDataMasker.mask(jsonMapper.writeValueAsString(data));
    }
}
