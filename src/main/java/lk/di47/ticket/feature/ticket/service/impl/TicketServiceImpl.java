package lk.di47.ticket.feature.ticket.service.impl;

import lk.di47.ticket.entity.AiPrediction;
import lk.di47.ticket.entity.Ticket;
import lk.di47.ticket.entity.TicketAttachment;
import lk.di47.ticket.entity.TicketReply;
import lk.di47.ticket.entity.TicketStatusHistory;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.ai.dto.AiPredictionRequest;
import lk.di47.ticket.feature.ai.dto.AiPredictionResponse;
import lk.di47.ticket.feature.ai.service.AiPredictionService;
import lk.di47.ticket.feature.notification.service.NotificationService;
import lk.di47.ticket.feature.ticket.dto.*;
import lk.di47.ticket.feature.ticket.service.TicketService;
import lk.di47.ticket.repository.AiPredictionRepository;
import lk.di47.ticket.repository.TicketAttachmentRepository;
import lk.di47.ticket.repository.TicketReplyRepository;
import lk.di47.ticket.repository.TicketRepository;
import lk.di47.ticket.repository.TicketStatusHistoryRepository;
import lk.di47.ticket.util.enums.NotificationType;
import lk.di47.ticket.util.enums.TicketPriority;
import lk.di47.ticket.util.enums.TicketStatus;
import lk.di47.ticket.util.generator.TicketNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final TicketAttachmentRepository ticketAttachmentRepository;
    private final TicketReplyRepository ticketReplyRepository;
    private final TicketStatusHistoryRepository ticketStatusHistoryRepository;
    private final AiPredictionRepository aiPredictionRepository;
    private final AiPredictionService aiPredictionService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTicketNo(TicketNumberGenerator.generate());
        ticket.setCustomerId(request.userId());
        ticket.setSubject(request.subject());
        ticket.setDescription(request.description());
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setCreatedAt(LocalDateTime.now());

        AiPredictionResponse prediction = predictTicketSafely(request);
        applyPrediction(ticket, prediction);

        Ticket savedTicket = ticketRepository.save(ticket);
        saveAiPrediction(savedTicket.getId(), prediction);
        saveHistory(savedTicket.getId(), null, TicketStatus.NEW, request.userId(), "Ticket created");

        notificationService.notifyUserAsync(
                request.userId(),
                "Ticket Created",
                "Your ticket " + savedTicket.getTicketNo() + " has been created successfully.",
                NotificationType.TICKET_CREATED
        );
        return toResponse(savedTicket);
    }

    @Override
    public List<TicketResponse> getTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        Map<Long, List<TicketAttachmentSummary>> attachmentsByTicketId = loadAttachmentsByTicketId(tickets);
        return tickets.stream()
                .map(ticket -> toResponse(ticket, attachmentsByTicketId.getOrDefault(ticket.getId(), List.of())))
                .toList();
    }

    @Override
    public TicketResponse getTicket(TicketDetailRequest request) {
        Ticket ticket = findTicket(request.ticketId());
        return toResponse(ticket, loadAttachments(ticket.getId()), loadReplies(ticket.getId()), loadTracking(ticket.getId()));
    }

    @Override
    @Transactional
    public TicketResponse updateStatus(UpdateTicketStatusRequest request) {
        Ticket ticket = findTicket(request.ticketId());
        TicketStatus previousStatus = ticket.getStatus();
        ticket.setStatus(request.status());
        ticket.setUpdatedAt(LocalDateTime.now());
        Ticket savedTicket = ticketRepository.save(ticket);
        saveHistory(request.ticketId(), previousStatus, request.status(), request.userId(), request.remark());
        notificationService.notifyUserAsync(
                ticket.getCustomerId(),
                "Ticket Status Updated",
                "Your ticket " + ticket.getTicketNo() + " status changed to " + request.status().name() + ".",
                NotificationType.TICKET_RESOLVED
        );
        return toResponse(savedTicket);
    }

    @Override
    @Transactional
    public TicketResponse assignTicket(AssignTicketRequest request) {
        Ticket ticket = findTicket(request.ticketId());
        TicketStatus previousStatus = ticket.getStatus();
        ticket.setDepartmentId(request.departmentId());
        ticket.setAssignedStaffId(request.assignedStaffId());
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket.setUpdatedAt(LocalDateTime.now());
        Ticket savedTicket = ticketRepository.save(ticket);
        saveHistory(request.ticketId(), previousStatus, TicketStatus.ASSIGNED, request.userId(), "Ticket assigned");
        notificationService.notifyUserAsync(
                ticket.getCustomerId(),
                "Ticket Assigned",
                "Your ticket " + ticket.getTicketNo() + " has been assigned.",
                NotificationType.TICKET_ASSIGNED
        );
        return toResponse(savedTicket);
    }

    @Override
    @Transactional
    public Long addReply(AddTicketReplyRequest request) {
        Ticket ticket = findTicket(request.ticketId());
        TicketReply reply = new TicketReply();
        reply.setTicketId(request.ticketId());
        reply.setSenderUserId(request.userId());
        reply.setMessage(request.message());
        reply.setCreatedAt(LocalDateTime.now());
        Long replyId = ticketReplyRepository.save(reply).getId();
        notificationService.notifyUserAsync(
                ticket.getCustomerId(),
                "Ticket Reply Added",
                "A new reply has been added to ticket " + ticket.getTicketNo() + ".",
                NotificationType.TICKET_REPLIED
        );
        return replyId;
    }

    private AiPredictionResponse predictTicketSafely(CreateTicketRequest request) {
        try {
            return aiPredictionService.predictTicket(new AiPredictionRequest(request.userId(), request.description()));
        } catch (Exception exception) {
            log.error("AI prediction service failed. Ticket will be created with default priority. Reason: {}",
                    exception.getMessage(),
                    exception);
            return null;
        }
    }

    private void applyPrediction(Ticket ticket, AiPredictionResponse prediction) {
        if (prediction == null) {
            return;
        }
        if (prediction.category() != null && !prediction.category().isBlank()) {
            ticket.setCategory(prediction.category());
        }
        if (prediction.suggestedDepartmentId() != null) {
            ticket.setDepartmentId(prediction.suggestedDepartmentId());
        }
        resolvePriority(prediction.priority()).ifPresent(ticket::setPriority);
    }

    private java.util.Optional<TicketPriority> resolvePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(TicketPriority.valueOf(priority.trim().toUpperCase()));
        } catch (IllegalArgumentException exception) {
            log.error("Invalid AI priority received: {}", priority, exception);
            return java.util.Optional.empty();
        }
    }

    private void saveAiPrediction(Long ticketId, AiPredictionResponse prediction) {
        if (prediction == null) {
            return;
        }

        AiPrediction aiPrediction = new AiPrediction();
        aiPrediction.setTicketId(ticketId);
        aiPrediction.setPredictedCategory(prediction.category());
        aiPrediction.setPredictedPriority(prediction.priority());
        aiPrediction.setSuggestedDepartmentId(prediction.suggestedDepartmentId());
        if (prediction.confidenceScore() != null) {
            aiPrediction.setConfidenceScore(BigDecimal.valueOf(prediction.confidenceScore()));
        }
        aiPrediction.setRawResponse(toJson(prediction));
        aiPrediction.setCreatedAt(LocalDateTime.now());
        aiPredictionRepository.save(aiPrediction);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            log.error("Unable to serialize AI prediction response: {}", exception.getMessage(), exception);
            return null;
        }
    }

    private Ticket findTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));
    }

    private void saveHistory(Long ticketId, TicketStatus previousStatus, TicketStatus newStatus, Long userId, String remark) {
        TicketStatusHistory history = new TicketStatusHistory();
        history.setTicketId(ticketId);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedByUserId(userId);
        history.setRemark(remark);
        history.setCreatedAt(LocalDateTime.now());
        ticketStatusHistoryRepository.save(history);
    }

    private TicketResponse toResponse(Ticket ticket) {
        return toResponse(ticket, loadAttachments(ticket.getId()));
    }

    private TicketResponse toResponse(Ticket ticket, List<TicketAttachmentSummary> attachments) {
        return toResponse(ticket, attachments, List.of(), List.of());
    }

    private TicketResponse toResponse(Ticket ticket,
                                      List<TicketAttachmentSummary> attachments,
                                      List<TicketReplyResponse> replies,
                                      List<TicketTrackingResponse> tracking) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketNo(),
                ticket.getCustomerId(),
                ticket.getDepartmentId(),
                ticket.getAssignedStaffId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                attachments,
                replies,
                tracking
        );
    }

    private List<TicketAttachmentSummary> loadAttachments(Long ticketId) {
        return ticketAttachmentRepository.findByTicketId(ticketId).stream()
                .map(this::toAttachmentSummary)
                .toList();
    }

    private Map<Long, List<TicketAttachmentSummary>> loadAttachmentsByTicketId(List<Ticket> tickets) {
        List<Long> ticketIds = tickets.stream().map(Ticket::getId).toList();
        if (ticketIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return ticketAttachmentRepository.findByTicketIdIn(ticketIds).stream()
                .collect(Collectors.groupingBy(
                        TicketAttachment::getTicketId,
                        Collectors.mapping(this::toAttachmentSummary, Collectors.toList())
                ));
    }

    private TicketAttachmentSummary toAttachmentSummary(TicketAttachment attachment) {
        return new TicketAttachmentSummary(attachment.getId(), attachment.getOriginalFileName());
    }

    private List<TicketReplyResponse> loadReplies(Long ticketId) {
        return ticketReplyRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(this::toReplyResponse)
                .toList();
    }

    private TicketReplyResponse toReplyResponse(TicketReply reply) {
        return new TicketReplyResponse(
                reply.getId(),
                reply.getSenderUserId(),
                reply.getMessage(),
                reply.getCreatedAt()
        );
    }

    private List<TicketTrackingResponse> loadTracking(Long ticketId) {
        return ticketStatusHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(this::toTrackingResponse)
                .toList();
    }

    private TicketTrackingResponse toTrackingResponse(TicketStatusHistory history) {
        return new TicketTrackingResponse(
                history.getId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getChangedByUserId(),
                history.getRemark(),
                history.getCreatedAt()
        );
    }

}
