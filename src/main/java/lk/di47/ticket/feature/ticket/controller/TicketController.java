package lk.di47.ticket.feature.ticket.controller;

import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.endpoint.TicketEndpoint;
import lk.di47.ticket.feature.ticket.dto.*;
import lk.di47.ticket.feature.ticket.service.TicketService;
import lk.di47.ticket.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
public class TicketController {
    private final TicketService ticketService;
    private final JsonMapper jsonMapper;

    @PostMapping(TicketEndpoint.CREATE)
    public ApiResponse<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        log.debug("Ticket Controller -> {}",this.toJson(request));
        return ApiResponse.success(MessageConstant.CREATED, ticketService.createTicket(request));
    }

    @PostMapping(TicketEndpoint.LIST)
    public ApiResponse<List<TicketResponse>> getTickets(@Valid @RequestBody ListTicketRequest request) {
        return ApiResponse.success(MessageConstant.SUCCESS, ticketService.getTickets());
    }

    @PostMapping(TicketEndpoint.DETAIL)
    public ApiResponse<TicketResponse> getTicket(@Valid @RequestBody TicketDetailRequest request) {
        return ApiResponse.success(MessageConstant.SUCCESS, ticketService.getTicket(request));
    }

    @PostMapping(TicketEndpoint.UPDATE_STATUS)
    public ApiResponse<TicketResponse> updateTicketStatus(@Valid @RequestBody UpdateTicketStatusRequest request) {
        return ApiResponse.success(MessageConstant.UPDATED, ticketService.updateStatus(request));
    }

    @PostMapping(TicketEndpoint.ASSIGN)
    public ApiResponse<TicketResponse> assignTicket(@Valid @RequestBody AssignTicketRequest request) {
        return ApiResponse.success(MessageConstant.UPDATED, ticketService.assignTicket(request));
    }

    @PostMapping(TicketEndpoint.ADD_REPLY)
    public ApiResponse<Long> addReply(@Valid @RequestBody AddTicketReplyRequest request) {
        return ApiResponse.success(MessageConstant.CREATED, ticketService.addReply(request));
    }

    @PostMapping(value = TicketEndpoint.UPLOAD_ATTACHMENT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<TicketAttachmentResponse> uploadAttachment(@RequestParam Long userId,
                                                                  @RequestParam(required = false) Long ticketId,
                                                                  @RequestParam MultipartFile attachment) {
        return ApiResponse.success(MessageConstant.CREATED, ticketService.uploadAttachment(userId, ticketId, attachment));
    }

    private String toJson(Object data){
      return   jsonMapper.writeValueAsString(data);
    }
}
