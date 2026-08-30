package lk.di47.ticket.feature.ticket.controller;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.SecurityConstant;
import lk.di47.ticket.constant.endpoint.TicketEndpoint;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.feature.ticket.dto.*;
import lk.di47.ticket.feature.ticket.service.TicketAttachmentService;
import lk.di47.ticket.feature.ticket.service.TicketService;
import lk.di47.ticket.response.ApiResponse;
import lk.di47.ticket.response.PageResponse;
import lk.di47.ticket.util.mask.SensitiveDataMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequiredArgsConstructor
@Log4j2
public class TicketController {
    private final TicketService ticketService;
    private final TicketAttachmentService ticketAttachmentService;
    private final JsonMapper jsonMapper;

    @PostMapping(TicketEndpoint.CREATE)
    public ApiResponse<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request,
                                                    HttpServletRequest servletRequest) {
        log.debug("Create Ticket -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.CREATED, ticketService.createTicket(request, currentUserId(servletRequest)));
    }

    @PostMapping(TicketEndpoint.LIST)
    public ApiResponse<PageResponse<TicketResponse>> getTickets(@Valid @RequestBody ListTicketRequest request,
                                                                HttpServletRequest servletRequest) {
        log.debug("List Ticket -> {}", this.toJson(request));
        validateCurrentUser(request.userId(), servletRequest);
        return ApiResponse.success(MessageConstant.SUCCESS, ticketService.getTickets(request));
    }

    @PostMapping(TicketEndpoint.DETAIL)
    public ApiResponse<TicketResponse> getTicket(@Valid @RequestBody TicketDetailRequest request,
                                                 HttpServletRequest servletRequest) {
        log.debug("Detail Ticket -> {}", this.toJson(request));
        validateCurrentUser(request.userId(), servletRequest);
        return ApiResponse.success(MessageConstant.SUCCESS, ticketService.getTicket(request));
    }

    @PostMapping(TicketEndpoint.UPDATE_STATUS)
    public ApiResponse<TicketResponse> updateTicketStatus(@Valid @RequestBody UpdateTicketStatusRequest request,
                                                          HttpServletRequest servletRequest) {
        log.debug("Update Ticket Status -> {}", this.toJson(request));
        validateCurrentUser(request.userId(), servletRequest);
        return ApiResponse.success(MessageConstant.UPDATED, ticketService.updateStatus(request));
    }

    @PostMapping(TicketEndpoint.ASSIGN)
    public ApiResponse<TicketResponse> assignTicket(@Valid @RequestBody AssignTicketRequest request,
                                                    HttpServletRequest servletRequest) {
        log.debug("Assign Ticket -> {}", this.toJson(request));
        validateCurrentUser(request.userId(), servletRequest);
        return ApiResponse.success(MessageConstant.UPDATED, ticketService.assignTicket(request));
    }

    @PostMapping(TicketEndpoint.ADD_REPLY)
    public ApiResponse<Long> addReply(@Valid @RequestBody AddTicketReplyRequest request,
                                      HttpServletRequest servletRequest) {
        log.debug("Add Ticket Reply -> {}", this.toJson(request));
        validateCurrentUser(request.userId(), servletRequest);
        return ApiResponse.success(MessageConstant.CREATED, ticketService.addReply(request));
    }

    @PostMapping(value = TicketEndpoint.UPLOAD_ATTACHMENT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<TicketAttachmentResponse> uploadAttachment(@RequestParam Long userId,
                                                                  @RequestParam(required = false) Long ticketId,
                                                                  @RequestParam MultipartFile attachment,
                                                                  HttpServletRequest servletRequest) {
        validateCurrentUser(userId, servletRequest);
        return ApiResponse.success(MessageConstant.CREATED, ticketAttachmentService.uploadAttachment(userId, ticketId, attachment));
    }

    @GetMapping(TicketEndpoint.DOWNLOAD_ATTACHMENT)
    public ResponseEntity<Resource> downloadAttachment(@RequestParam Long id,
                                                       HttpServletRequest servletRequest) {
        Long currentUserId = currentUserId(servletRequest);
        TicketAttachmentDownload attachment = ticketAttachmentService.downloadAttachment(id, currentUserId);
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(attachment.originalFileName())
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.contentType()))
                .contentLength(attachment.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .header("X-Attachment-Id", String.valueOf(attachment.id()))
                .header("X-File-Name", attachment.originalFileName())
                .body(attachment.resource());
    }

    private String toJson(Object data) {
        return SensitiveDataMasker.mask(jsonMapper.writeValueAsString(data));
    }

    private void validateCurrentUser(Long requestUserId, HttpServletRequest servletRequest) {
        Long currentUserId = currentUserId(servletRequest);
        if (!currentUserId.equals(requestUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Request user mismatch");
        }
    }

    private Long currentUserId(HttpServletRequest servletRequest) {
        Object currentUserId = servletRequest.getAttribute(SecurityConstant.CURRENT_USER_ID);
        if (currentUserId instanceof Long value) {
            return value;
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "Missing authenticated user");
    }
}
