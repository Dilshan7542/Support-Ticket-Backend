package lk.di47.ticket.feature.ticket.service.impl;

import lk.di47.ticket.entity.AiPrediction;
import lk.di47.ticket.client.ai.AiPredictionClientProperties;
import lk.di47.ticket.entity.Vendor;
import lk.di47.ticket.entity.Department;
import lk.di47.ticket.entity.Ticket;
import lk.di47.ticket.entity.TicketAttachment;
import lk.di47.ticket.entity.TicketCategory;
import lk.di47.ticket.entity.TicketCategoryDepartmentMapping;
import lk.di47.ticket.entity.TicketPriorityMaster;
import lk.di47.ticket.entity.TicketReply;
import lk.di47.ticket.entity.TicketStatusHistory;
import lk.di47.ticket.entity.User;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.ai.dto.AiPredictionRequest;
import lk.di47.ticket.feature.ai.dto.AiPredictionResponse;
import lk.di47.ticket.feature.ai.service.AiPredictionService;
import lk.di47.ticket.feature.notification.service.NotificationService;
import lk.di47.ticket.feature.ticket.dto.*;
import lk.di47.ticket.feature.ticket.service.TicketService;
import lk.di47.ticket.repository.AiPredictionRepository;
import lk.di47.ticket.repository.VendorRepository;
import lk.di47.ticket.repository.DepartmentRepository;
import lk.di47.ticket.repository.TicketAttachmentRepository;
import lk.di47.ticket.repository.TicketCategoryDepartmentMappingRepository;
import lk.di47.ticket.repository.TicketCategoryRepository;
import lk.di47.ticket.repository.TicketPriorityMasterRepository;
import lk.di47.ticket.repository.TicketReplyRepository;
import lk.di47.ticket.repository.TicketRepository;
import lk.di47.ticket.repository.TicketStatusHistoryRepository;
import lk.di47.ticket.repository.UserRepository;
import lk.di47.ticket.response.PageResponse;
import lk.di47.ticket.util.PaginationUtil;
import lk.di47.ticket.util.enums.NotificationType;
import lk.di47.ticket.util.enums.Status;
import lk.di47.ticket.util.enums.TicketStatus;
import lk.di47.ticket.util.enums.UserRole;
import lk.di47.ticket.util.generator.TicketNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final TicketAttachmentRepository ticketAttachmentRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final VendorRepository vendorRepository;
    private final DepartmentRepository departmentRepository;
    private final TicketReplyRepository ticketReplyRepository;
    private final TicketStatusHistoryRepository ticketStatusHistoryRepository;
    private final UserRepository userRepository;
    private final AiPredictionRepository aiPredictionRepository;
    private final AiPredictionService aiPredictionService;
    private final TicketCategoryDepartmentMappingRepository mappingRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final TicketPriorityMasterRepository ticketPriorityMasterRepository;
    private final AiPredictionClientProperties aiPredictionClientProperties;

    @Override
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, Long currentUserId) {
        String subject = requireText(request.subject(), "Subject is required");
        String description = requireText(request.description(), "Description is required");
        User customer = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        AiPredictionResponse prediction = predictTicket(subject, description);
        TicketCategory category = resolveActiveCategory(prediction.category());
        TicketPriorityMaster priority = resolveActivePriority(prediction.priority());
        TicketCategoryDepartmentMapping mapping = mappingRepository
                .findByCategoryIdAndStatus(category.getId(), Status.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "Department mapping is not configured"));
        Department department = validateActiveDepartment(mapping.getDepartmentId());
        boolean requiresManualReview = requiresManualReview(prediction);

        Ticket ticket = new Ticket();
        ticket.setTicketNo(TicketNumberGenerator.generate());
        ticket.setCustomerId(customer.getId());
        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setCategoryId(category.getId());
        ticket.setCategoryCode(category.getCode());
        ticket.setDepartmentId(department.getId());
        ticket.setVendorId(department.getVendorId());
        ticket.setPriority(priority.getCode());
        ticket.setRequiresManualReview(requiresManualReview);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setCreatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);
        saveAiPrediction(savedTicket.getId(), prediction);
        saveHistory(savedTicket.getId(), null, TicketStatus.NEW, customer.getId(), "Ticket created");

        notificationService.notifyUserAsync(
                customer.getId(),
                "Ticket Created",
                "Your ticket " + savedTicket.getTicketNo() + " has been created successfully.",
                NotificationType.TICKET_CREATED
        );
        return toResponse(savedTicket);
    }

    @Override
    public PageResponse<TicketResponse> getTickets(ListTicketRequest request) {
        UserRole requesterRole = resolveUserRole(request.userId());
        Specification<Ticket> specification = buildTicketListSpecification(request, requesterRole);
        Page<Ticket> tickets = ticketRepository.findAll(
                specification,
                PaginationUtil.toPageable(request.page(), request.size())
        );
        Map<Long, List<TicketAttachmentSummary>> attachmentsByTicketId = loadAttachmentsByTicketId(tickets.getContent());
        return PageResponse.from(
                tickets,
                ticket -> toResponse(ticket, attachmentsByTicketId.getOrDefault(ticket.getId(), List.of()))
        );
    }

    private Specification<Ticket> buildTicketListSpecification(ListTicketRequest request, UserRole requesterRole) {
        Specification<Ticket> specification = Specification.unrestricted();
        if (requesterRole == UserRole.CUSTOMER) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("customerId"), request.userId()));
        } else if (request.customerId() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("customerId"), request.customerId()));
        }

        specification = specification
                .and(equalIfPresent("id", request.ticketId()))
                .and(likeIfPresent("ticketNo", request.ticketNo()))
                .and(equalIfPresent("vendorId", request.vendorId()))
                .and(equalIfPresent("departmentId", request.departmentId()))
                .and(equalIfPresent("assignedStaffId", request.assignedStaffId()))
                .and(equalIfPresent("categoryId", request.categoryId()))
                .and(likeIfPresent("categoryCode", request.categoryCode()))
                .and(likeIfPresent("subject", request.subject()))
                .and(equalIfPresent("priority", request.priority()))
                .and(equalIfPresent("status", request.status()));

        specification = specification
                .and(inIfTextPresent("customerId", request.customerName(), this::findUserIdsByName))
                .and(inIfTextPresent("vendorId", request.vendorName(), this::findVendorIdsByName))
                .and(inIfTextPresent("departmentId", request.departmentName(), this::findDepartmentIdsByName))
                .and(inIfTextPresent("assignedStaffId", request.assignedStaffName(), this::findUserIdsByName))
                .and(inIfTextPresent("categoryId", request.categoryName(), this::findCategoryIdsByName));
        return specification;
    }

    private <T> Specification<Ticket> equalIfPresent(String field, T value) {
        if (value == null) {
            return Specification.unrestricted();
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(field), value);
    }

    private Specification<Ticket> likeIfPresent(String field, String value) {
        if (isBlank(value)) {
            return Specification.unrestricted();
        }
        String pattern = "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get(field)), pattern);
    }

    private Specification<Ticket> inIfTextPresent(String field,
                                                  String value,
                                                  java.util.function.Function<String, List<Long>> idResolver) {
        if (isBlank(value)) {
            return Specification.unrestricted();
        }
        List<Long> ids = idResolver.apply(value.trim());
        if (ids.isEmpty()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        }
        return (root, query, criteriaBuilder) -> root.get(field).in(ids);
    }

    private List<Long> findUserIdsByName(String name) {
        return userRepository.findByFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(name, name).stream()
                .map(User::getId)
                .toList();
    }

    private List<Long> findVendorIdsByName(String name) {
        return vendorRepository.findByNameContainingIgnoreCase(name).stream()
                .map(Vendor::getId)
                .toList();
    }

    private List<Long> findDepartmentIdsByName(String name) {
        return departmentRepository.findByNameContainingIgnoreCase(name).stream()
                .map(Department::getId)
                .toList();
    }

    private List<Long> findCategoryIdsByName(String name) {
        return ticketCategoryRepository.findByNameContainingIgnoreCase(name).stream()
                .map(TicketCategory::getId)
                .toList();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public TicketResponse getTicket(TicketDetailRequest request) {
        Ticket ticket = findTicket(request.ticketId());
        validateCustomerTicketAccess(request.userId(), ticket);
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
        TicketCategory selectedCategory = null;
        if (!isBlank(request.categoryCode())) {
            selectedCategory = resolveActiveCategory(request.categoryCode());
            ticket.setCategoryId(selectedCategory.getId());
            ticket.setCategoryCode(selectedCategory.getCode());
        }
        if (!isBlank(request.priority())) {
            ticket.setPriority(resolveActivePriority(request.priority()).getCode());
        }
        applyDepartmentMapping(ticket, request.departmentId());
        validateCategoryDepartmentMapping(selectedCategory, ticket.getDepartmentId());
        validateAssignedStaff(request.assignedStaffId(), ticket.getVendorId());
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
        validateCustomerTicketAccess(request.userId(), ticket);
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

    private AiPredictionResponse predictTicket(String subject, String description) {
        try {
            AiPredictionRequest predictionRequest = new AiPredictionRequest(subject, description);
            log.info("AI Predication Request -> {} ",this.toJson(predictionRequest));
            AiPredictionResponse predictionResponse = aiPredictionService.predictTicket(new AiPredictionRequest(subject, description));
            log.info("AI Predication Response -> {}",this.toJson(predictionResponse));
            if (predictionResponse == null) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "AI prediction service returned no prediction");
            }
            return predictionResponse;
        } catch (Exception exception) {
            if (exception instanceof BusinessException businessException) {
                throw businessException;
            }
            log.error("AI prediction service failed. Reason: {}",
                    exception.getMessage(),
                    exception);
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "AI prediction service is unavailable", exception);
        }
    }

    private void applyDepartmentMapping(Ticket ticket, Long departmentId) {
        Department department = validateActiveDepartment(departmentId);
        ticket.setDepartmentId(department.getId());
        ticket.setVendorId(department.getVendorId());
    }

    private void validateCategoryDepartmentMapping(TicketCategory category, Long departmentId) {
        if (category == null || departmentId == null) {
            return;
        }
        TicketCategoryDepartmentMapping mapping = mappingRepository.findByCategoryIdAndStatus(category.getId(), Status.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "Department mapping is not configured"));
        if (!departmentId.equals(mapping.getDepartmentId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Selected category does not belong to selected department");
        }
    }

    private void validateAssignedStaff(Long assignedStaffId, Long vendorId) {
        if (assignedStaffId == null) {
            return;
        }
        User assignedStaff = userRepository.findById(assignedStaffId)
                .orElseThrow(() -> new NotFoundException("Assigned user not found"));
        if (assignedStaff.getStatus() != Status.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Assigned user is not active");
        }
        if (vendorId != null && !vendorId.equals(assignedStaff.getVendorId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Assigned user does not belong to department vendor");
        }
    }

    private Vendor validateActiveVendor(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new NotFoundException("Vendor not found"));
        if (vendor.getStatus() != Status.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Vendor is not active");
        }
        return vendor;
    }

    private TicketCategory resolveActiveCategory(String categoryCode) {
        String normalizedCategory = requireText(categoryCode, "Predicted ticket category is missing")
                .trim()
                .toUpperCase(Locale.ROOT);
        return ticketCategoryRepository.findByCodeAndStatus(normalizedCategory, Status.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "Predicted ticket category is not configured"));
    }

    private TicketPriorityMaster resolveActivePriority(String priorityCode) {
        String normalizedPriority = requireText(priorityCode, "Predicted ticket priority is missing")
                .trim()
                .toUpperCase(Locale.ROOT);
        return ticketPriorityMasterRepository.findByCodeAndStatus(normalizedPriority, Status.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "Predicted ticket priority is not configured"));
    }

    private Department validateActiveDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found"));
        if (department.getStatus() != Status.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Department is not active");
        }
        return department;
    }

    private boolean requiresManualReview(AiPredictionResponse prediction) {
        double threshold = aiPredictionServiceThreshold();
        return Boolean.TRUE.equals(prediction.requiresManualReview())
                || prediction.categoryConfidence() == null
                || prediction.priorityConfidence() == null
                || prediction.categoryConfidence() < threshold
                || prediction.priorityConfidence() < threshold;
    }

    private double aiPredictionServiceThreshold() {
        return aiPredictionClientProperties.getConfidenceThreshold();
    }

    private void saveAiPrediction(Long ticketId, AiPredictionResponse prediction) {
        if (prediction == null) {
            return;
        }

        AiPrediction aiPrediction = new AiPrediction();
        aiPrediction.setTicketId(ticketId);
        aiPrediction.setPredictedCategory(prediction.category());
        aiPrediction.setPredictedPriority(prediction.priority());
        if (prediction.categoryConfidence() != null) {
            aiPrediction.setCategoryConfidence(BigDecimal.valueOf(prediction.categoryConfidence()));
        }
        if (prediction.priorityConfidence() != null) {
            aiPrediction.setPriorityConfidence(BigDecimal.valueOf(prediction.priorityConfidence()));
        }
        aiPrediction.setRequiresManualReview(Boolean.TRUE.equals(prediction.requiresManualReview()));
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

    private UserRole resolveUserRole(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"))
                .getRole();
    }

    private void validateCustomerTicketAccess(Long userId, Ticket ticket) {
        if (resolveUserRole(userId) == UserRole.CUSTOMER && !userId.equals(ticket.getCustomerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "You do not have access to this ticket");
        }
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
        return toResponse(ticket, loadAttachments(ticket.getId()), loadReplies(ticket.getId()), loadTracking(ticket.getId()));
    }

    private TicketResponse toResponse(Ticket ticket, List<TicketAttachmentSummary> attachments) {
        return toResponse(ticket, attachments, List.of(), List.of());
    }

    private TicketResponse toResponse(Ticket ticket,
                                      List<TicketAttachmentSummary> attachments,
                                      List<TicketReplyResponse> replies,
                                      List<TicketTrackingResponse> tracking) {
        TicketRelations relations = resolveRelations(ticket);
        java.util.Optional<AiPrediction> latestPrediction = latestPrediction(ticket.getId());
        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketNo(),
                ticket.getCustomerId(),
                relations.vendorId(),
                relations.vendorName(),
                relations.departmentId(),
                relations.departmentName(),
                relations.categoryId(),
                ticket.getAssignedStaffId(),
                resolveUserName(ticket.getAssignedStaffId()),
                ticket.getSubject(),
                ticket.getDescription(),
                relations.categoryCode(),
                relations.categoryName(),
                ticket.getPriority(),
                latestPrediction.map(AiPrediction::getCategoryConfidence).map(BigDecimal::doubleValue).orElse(null),
                latestPrediction.map(AiPrediction::getPriorityConfidence).map(BigDecimal::doubleValue).orElse(null),
                ticket.getRequiresManualReview(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                attachments,
                replies,
                tracking
        );
    }

    private TicketRelations resolveRelations(Ticket ticket) {
        TicketCategory category = resolveCategory(ticket);
        Department department = resolveDepartment(ticket, category);
        Long vendorId = ticket.getVendorId();
        if (vendorId == null && department != null) {
            vendorId = department.getVendorId();
        }
        Vendor vendor = vendorId == null ? null : vendorRepository.findById(vendorId).orElse(null);
        return new TicketRelations(
                vendorId,
                vendor == null ? null : vendor.getName(),
                department == null ? ticket.getDepartmentId() : department.getId(),
                department == null ? null : department.getName(),
                category == null ? ticket.getCategoryId() : category.getId(),
                category == null ? ticket.getCategoryCode() : category.getCode(),
                category == null ? null : category.getName()
        );
    }

    private TicketCategory resolveCategory(Ticket ticket) {
        if (ticket.getCategoryId() != null) {
            return ticketCategoryRepository.findById(ticket.getCategoryId()).orElse(null);
        }
        if (ticket.getCategoryCode() == null || ticket.getCategoryCode().isBlank()) {
            return null;
        }
        return ticketCategoryRepository.findByCodeAndStatus(ticket.getCategoryCode(), Status.ACTIVE)
                .orElse(null);
    }

    private String resolveUserName(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
                .map(User::getFullName)
                .orElse(null);
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, message);
        }
        return value.trim();
    }

    private Department resolveDepartment(Ticket ticket, TicketCategory category) {
        Long departmentId = ticket.getDepartmentId();
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId).orElse(null);
    }

    private java.util.Optional<AiPrediction> latestPrediction(Long ticketId) {
        return aiPredictionRepository.findTopByTicketIdOrderByCreatedAtDesc(ticketId);
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

    private record TicketRelations(
            Long vendorId,
            String vendorName,
            Long departmentId,
            String departmentName,
            Long categoryId,
            String categoryCode,
            String categoryName
    ) {
    }

}
