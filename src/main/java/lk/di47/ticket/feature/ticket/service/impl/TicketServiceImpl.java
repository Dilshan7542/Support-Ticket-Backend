package lk.di47.ticket.feature.ticket.service.impl;

import lk.di47.ticket.entity.AiPrediction;
import lk.di47.ticket.entity.Ticket;
import lk.di47.ticket.entity.TicketAttachment;
import lk.di47.ticket.entity.TicketReply;
import lk.di47.ticket.entity.TicketStatusHistory;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.ai.dto.AiPredictionRequest;
import lk.di47.ticket.feature.ai.dto.AiPredictionResponse;
import lk.di47.ticket.feature.ai.service.AiPredictionService;
import lk.di47.ticket.feature.notification.service.NotificationService;
import lk.di47.ticket.feature.ticket.config.TicketAttachmentProperties;
import lk.di47.ticket.feature.ticket.dto.*;
import lk.di47.ticket.feature.ticket.service.TicketService;
import lk.di47.ticket.repository.AiPredictionRepository;
import lk.di47.ticket.repository.TicketAttachmentRepository;
import lk.di47.ticket.repository.TicketReplyRepository;
import lk.di47.ticket.repository.TicketRepository;
import lk.di47.ticket.repository.TicketStatusHistoryRepository;
import lk.di47.ticket.repository.UserRepository;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final TicketReplyRepository ticketReplyRepository;
    private final TicketStatusHistoryRepository ticketStatusHistoryRepository;
    private final TicketAttachmentRepository ticketAttachmentRepository;
    private final UserRepository userRepository;
    private final AiPredictionRepository aiPredictionRepository;
    private final AiPredictionService aiPredictionService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final TicketAttachmentProperties attachmentProperties;

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
        return ticketRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public TicketResponse getTicket(TicketDetailRequest request) {
        return toResponse(findTicket(request.ticketId()));
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

    @Override
    @Transactional
    public TicketAttachmentResponse uploadAttachment(Long userId, Long ticketId, org.springframework.web.multipart.MultipartFile file) {
        validateAttachmentRequest(userId, ticketId, file);

        String originalFileName = sanitizeFileName(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + resolveExtension(originalFileName);
        Path targetDirectory = resolveTargetDirectory(ticketId, userId);
        Path targetPath = targetDirectory.resolve(storedFileName);

        try {
            Files.createDirectories(targetDirectory);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Unable to save attachment");
        }

        TicketAttachment attachment = new TicketAttachment();
        attachment.setTicketId(ticketId);
        attachment.setUploadedByUserId(userId);
        attachment.setOriginalFileName(originalFileName);
        attachment.setStoredFileName(storedFileName);
        attachment.setContentType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setStoragePath(targetPath.toString());
        attachment.setCreatedAt(LocalDateTime.now());
        return toAttachmentResponse(ticketAttachmentRepository.save(attachment));
    }

    private AiPredictionResponse predictTicketSafely(CreateTicketRequest request) {
        try {
            return aiPredictionService.predictTicket(new AiPredictionRequest(request.userId(), request.description()));
        } catch (Exception exception) {
            log.warn("AI prediction service failed. Ticket will be created with default priority. Reason: {}", exception.getMessage());
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
            log.warn("Invalid AI priority received: {}", priority);
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
            log.warn("Unable to serialize AI prediction response: {}", exception.getMessage());
            return null;
        }
    }

    private Ticket findTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));
    }

    private void validateAttachmentRequest(Long userId, Long ticketId, org.springframework.web.multipart.MultipartFile file) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "userId is required");
        }
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
        if (ticketId != null && !ticketRepository.existsById(ticketId)) {
            throw new NotFoundException("Ticket not found");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "attachment is required");
        }
        if (file.getSize() > attachmentProperties.getMaxFileSizeBytes()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Attachment exceeds max file size");
        }
    }

    private Path resolveTargetDirectory(Long ticketId, Long userId) {
        Path basePath = Path.of(attachmentProperties.getStoragePath()).toAbsolutePath().normalize();
        if (ticketId != null) {
            return basePath.resolve("tickets").resolve(String.valueOf(ticketId)).normalize();
        }
        return basePath.resolve("users").resolve(String.valueOf(userId)).normalize();
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "attachment";
        }
        String sanitized = Path.of(fileName).getFileName().toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        return sanitized.isBlank() ? "attachment" : sanitized;
    }

    private String resolveExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
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
                ticket.getCreatedAt()
        );
    }

    private TicketAttachmentResponse toAttachmentResponse(TicketAttachment attachment) {
        return new TicketAttachmentResponse(
                attachment.getId(),
                attachment.getTicketId(),
                attachment.getUploadedByUserId(),
                attachment.getOriginalFileName(),
                attachment.getStoredFileName(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getStoragePath(),
                attachment.getCreatedAt()
        );
    }
}
