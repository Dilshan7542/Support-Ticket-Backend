package lk.di47.ticket.feature.ticket.service.impl;

import lk.di47.ticket.entity.AiPrediction;
import lk.di47.ticket.entity.Company;
import lk.di47.ticket.entity.Department;
import lk.di47.ticket.entity.Ticket;
import lk.di47.ticket.entity.TicketAttachment;
import lk.di47.ticket.entity.TicketCategory;
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
import lk.di47.ticket.repository.CompanyRepository;
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
    private final CompanyRepository companyRepository;
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

    @Override
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        String subject = requireText(request.effectiveSubject(), "Subject is required");
        String description = requireText(request.effectiveDescription(), "Description is required");

        Ticket ticket = new Ticket();
        ticket.setTicketNo(TicketNumberGenerator.generate());
        ticket.setCustomerId(request.userId());
        if (request.companyId() != null) {
            ticket.setCompanyId(validateActiveCompany(request.companyId()).getId());
        }
        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setPriority("MEDIUM");
        ticket.setStatus(TicketStatus.NEW);
        ticket.setCreatedAt(LocalDateTime.now());

        AiPredictionResponse prediction = predictTicketSafely(request);
        applyPrediction(ticket, request.categoryCode(), prediction);

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
                .and(equalIfPresent("companyId", request.companyId()))
                .and(equalIfPresent("departmentId", request.departmentId()))
                .and(equalIfPresent("assignedStaffId", request.assignedStaffId()))
                .and(equalIfPresent("categoryId", request.categoryId()))
                .and(likeIfPresent("categoryCode", request.categoryCode()))
                .and(likeIfPresent("subject", request.subject()))
                .and(equalIfPresent("priority", request.priority()))
                .and(equalIfPresent("status", request.status()));

        specification = specification
                .and(inIfTextPresent("customerId", request.customerName(), this::findUserIdsByName))
                .and(inIfTextPresent("companyId", request.companyName(), this::findCompanyIdsByName))
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

    private List<Long> findCompanyIdsByName(String name) {
        return companyRepository.findByNameContainingIgnoreCase(name).stream()
                .map(Company::getId)
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
        applyDepartmentMapping(ticket, request.departmentId());
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

    private AiPredictionResponse predictTicketSafely(CreateTicketRequest request) {
        try {
            return aiPredictionService.predictTicket(new AiPredictionRequest(request.userId(), request.effectiveDescription()));
        } catch (Exception exception) {
            log.error("AI prediction service failed. Ticket will be created with default priority. Reason: {}",
                    exception.getMessage(),
                    exception);
            return null;
        }
    }

    private void applyPrediction(Ticket ticket, String requestedCategoryCode, AiPredictionResponse prediction) {
        String categoryCode = requestedCategoryCode;
        if ((categoryCode == null || categoryCode.isBlank()) && prediction != null) {
            categoryCode = prediction.category();
        }
        applyCategoryMapping(ticket, categoryCode);
        if (prediction != null) {
            resolvePriority(prediction.priority()).ifPresent(ticket::setPriority);
        }
    }

    private void applyCategoryMapping(Ticket ticket, String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            return;
        }
        TicketCategory category = ticketCategoryRepository.findByCodeAndStatus(categoryCode, Status.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "Ticket category not found"));
        ticket.setCategoryId(category.getId());
        ticket.setCategoryCode(category.getCode());
        if (ticket.getCompanyId() == null) {
            return;
        }
        mappingRepository.findByCompanyIdAndCategoryIdAndStatus(ticket.getCompanyId(), category.getId(), Status.ACTIVE)
                .ifPresent(mapping -> applyDepartmentMapping(ticket, mapping.getDepartmentId()));
    }

    private void applyDepartmentMapping(Ticket ticket, Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found"));
        if (department.getStatus() != Status.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Department is not active");
        }
        if (ticket.getCompanyId() != null && !ticket.getCompanyId().equals(department.getCompanyId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Department does not belong to ticket company");
        }
        ticket.setDepartmentId(department.getId());
        ticket.setCompanyId(department.getCompanyId());
    }

    private Company validateActiveCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found"));
        if (company.getStatus() != Status.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Company is not active");
        }
        return company;
    }

    private java.util.Optional<String> resolvePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return java.util.Optional.empty();
        }
        String normalizedPriority = priority.trim().toUpperCase(Locale.ROOT);
        if (ticketPriorityMasterRepository.findByCodeAndStatus(normalizedPriority, Status.ACTIVE).isPresent()) {
            return java.util.Optional.of(normalizedPriority);
        }
        log.error("Invalid AI priority received: {}", priority);
        return java.util.Optional.empty();
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
        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketNo(),
                ticket.getCustomerId(),
                relations.companyId(),
                relations.companyName(),
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
        Long companyId = ticket.getCompanyId();
        if (companyId == null && department != null) {
            companyId = department.getCompanyId();
        }
        Company company = companyId == null ? null : companyRepository.findById(companyId).orElse(null);
        return new TicketRelations(
                companyId,
                company == null ? null : company.getName(),
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
            Long companyId,
            String companyName,
            Long departmentId,
            String departmentName,
            Long categoryId,
            String categoryCode,
            String categoryName
    ) {
    }

}
