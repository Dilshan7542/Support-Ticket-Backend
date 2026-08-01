package lk.di47.ticket.feature.ticket.service.impl;

import lk.di47.ticket.client.ai.AiPredictionClientProperties;
import lk.di47.ticket.entity.AiPrediction;
import lk.di47.ticket.entity.Department;
import lk.di47.ticket.entity.Ticket;
import lk.di47.ticket.entity.TicketCategory;
import lk.di47.ticket.entity.TicketCategoryDepartmentMapping;
import lk.di47.ticket.entity.TicketPriorityMaster;
import lk.di47.ticket.entity.User;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.feature.ai.dto.AiPredictionRequest;
import lk.di47.ticket.feature.ai.dto.AiPredictionResponse;
import lk.di47.ticket.feature.ai.service.AiPredictionService;
import lk.di47.ticket.feature.notification.service.NotificationService;
import lk.di47.ticket.feature.ticket.dto.CreateTicketRequest;
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
import lk.di47.ticket.util.enums.Status;
import lk.di47.ticket.util.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TicketServiceImplTest {
    private TicketRepository ticketRepository;
    private TicketCategoryRepository ticketCategoryRepository;
    private DepartmentRepository departmentRepository;
    private UserRepository userRepository;
    private AiPredictionRepository aiPredictionRepository;
    private AiPredictionService aiPredictionService;
    private TicketCategoryDepartmentMappingRepository mappingRepository;
    private TicketPriorityMasterRepository priorityRepository;
    private TicketServiceImpl ticketService;
    private final AtomicReference<AiPrediction> savedPrediction = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        ticketCategoryRepository = mock(TicketCategoryRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        userRepository = mock(UserRepository.class);
        aiPredictionRepository = mock(AiPredictionRepository.class);
        aiPredictionService = mock(AiPredictionService.class);
        mappingRepository = mock(TicketCategoryDepartmentMappingRepository.class);
        priorityRepository = mock(TicketPriorityMasterRepository.class);

        AiPredictionClientProperties properties = new AiPredictionClientProperties();
        properties.setConfidenceThreshold(0.60);

        ticketService = new TicketServiceImpl(
                ticketRepository,
                mock(TicketAttachmentRepository.class),
                ticketCategoryRepository,
                mock(VendorRepository.class),
                departmentRepository,
                mock(TicketReplyRepository.class),
                mock(TicketStatusHistoryRepository.class),
                userRepository,
                aiPredictionRepository,
                aiPredictionService,
                mappingRepository,
                mock(NotificationService.class),
                JsonMapper.builder().build(),
                priorityRepository,
                properties
        );

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(1001L);
            return ticket;
        });
        when(aiPredictionRepository.save(any(AiPrediction.class))).thenAnswer(invocation -> {
            AiPrediction prediction = invocation.getArgument(0);
            savedPrediction.set(prediction);
            return prediction;
        });
        when(aiPredictionRepository.findTopByTicketIdOrderByCreatedAtDesc(1001L))
                .thenAnswer(invocation -> Optional.ofNullable(savedPrediction.get()));
        givenValidCustomer();
        givenValidMasterData("ORDER_NOT_RECEIVED", "HIGH");
    }

    @Test
    void createsTicketFromAiPredictionAndRoutesByCategoryMapping() {
        when(aiPredictionService.predictTicket(any(AiPredictionRequest.class)))
                .thenReturn(prediction("ORDER_NOT_RECEIVED", 0.91, "HIGH", 0.86, false));

        var response = ticketService.createTicket(new CreateTicketRequest("Missing order", "Order did not arrive"), 7L);

        ArgumentCaptor<AiPredictionRequest> aiRequest = ArgumentCaptor.forClass(AiPredictionRequest.class);
        verify(aiPredictionService).predictTicket(aiRequest.capture());
        assertThat(aiRequest.getValue().subject()).isEqualTo("Missing order");
        assertThat(aiRequest.getValue().description()).isEqualTo("Order did not arrive");

        ArgumentCaptor<Ticket> ticket = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticket.capture());
        assertThat(ticket.getValue().getCustomerId()).isEqualTo(7L);
        assertThat(ticket.getValue().getCategoryId()).isEqualTo(11L);
        assertThat(ticket.getValue().getDepartmentId()).isEqualTo(22L);
        assertThat(ticket.getValue().getVendorId()).isEqualTo(1L);
        assertThat(ticket.getValue().getPriority()).isEqualTo("HIGH");
        assertThat(ticket.getValue().getRequiresManualReview()).isFalse();

        AiPrediction audit = savedPrediction.get();
        assertThat(audit.getPredictedCategory()).isEqualTo("ORDER_NOT_RECEIVED");
        assertThat(audit.getCategoryConfidence()).isEqualByComparingTo(BigDecimal.valueOf(0.91));
        assertThat(audit.getPredictedPriority()).isEqualTo("HIGH");
        assertThat(audit.getPriorityConfidence()).isEqualByComparingTo(BigDecimal.valueOf(0.86));
        assertThat(audit.getRequiresManualReview()).isFalse();

        assertThat(response.departmentId()).isEqualTo(22L);
        assertThat(response.categoryCode()).isEqualTo("ORDER_NOT_RECEIVED");
        assertThat(response.priority()).isEqualTo("HIGH");
        assertThat(response.categoryConfidence()).isEqualTo(0.91);
        assertThat(response.priorityConfidence()).isEqualTo(0.86);
        assertThat(response.requiresManualReview()).isFalse();
    }

    @Test
    void rejectsWhenCategoryIsNotConfigured() {
        when(aiPredictionService.predictTicket(any(AiPredictionRequest.class)))
                .thenReturn(prediction("UNKNOWN_CATEGORY", 0.91, "HIGH", 0.86, false));
        when(ticketCategoryRepository.findByCodeAndStatus("UNKNOWN_CATEGORY", Status.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.createTicket(new CreateTicketRequest("Subject", "Description"), 7L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Predicted ticket category is not configured");
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void rejectsWhenPriorityIsNotConfigured() {
        when(aiPredictionService.predictTicket(any(AiPredictionRequest.class)))
                .thenReturn(prediction("ORDER_NOT_RECEIVED", 0.91, "UNKNOWN", 0.86, false));
        when(priorityRepository.findByCodeAndStatus("UNKNOWN", Status.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.createTicket(new CreateTicketRequest("Subject", "Description"), 7L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Predicted ticket priority is not configured");
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void rejectsWhenDepartmentMappingIsMissing() {
        when(aiPredictionService.predictTicket(any(AiPredictionRequest.class)))
                .thenReturn(prediction("ORDER_NOT_RECEIVED", 0.91, "HIGH", 0.86, false));
        when(mappingRepository.findByCategoryIdAndStatus(11L, Status.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.createTicket(new CreateTicketRequest("Subject", "Description"), 7L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Department mapping is not configured");
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void marksManualReviewForLowCategoryConfidence() {
        when(aiPredictionService.predictTicket(any(AiPredictionRequest.class)))
                .thenReturn(prediction("ORDER_NOT_RECEIVED", 0.59, "HIGH", 0.86, false));

        ticketService.createTicket(new CreateTicketRequest("Subject", "Description"), 7L);

        ArgumentCaptor<Ticket> ticket = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticket.capture());
        assertThat(ticket.getValue().getRequiresManualReview()).isTrue();
    }

    @Test
    void marksManualReviewForLowPriorityConfidence() {
        when(aiPredictionService.predictTicket(any(AiPredictionRequest.class)))
                .thenReturn(prediction("ORDER_NOT_RECEIVED", 0.91, "HIGH", 0.59, false));

        ticketService.createTicket(new CreateTicketRequest("Subject", "Description"), 7L);

        ArgumentCaptor<Ticket> ticket = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticket.capture());
        assertThat(ticket.getValue().getRequiresManualReview()).isTrue();
    }

    @Test
    void marksManualReviewWhenAiRequiresIt() {
        when(aiPredictionService.predictTicket(any(AiPredictionRequest.class)))
                .thenReturn(prediction("ORDER_NOT_RECEIVED", 0.91, "HIGH", 0.86, true));

        ticketService.createTicket(new CreateTicketRequest("Subject", "Description"), 7L);

        ArgumentCaptor<Ticket> ticket = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticket.capture());
        assertThat(ticket.getValue().getRequiresManualReview()).isTrue();
    }

    @Test
    void returnsControlledErrorWhenAiServiceFails() {
        when(aiPredictionService.predictTicket(any(AiPredictionRequest.class))).thenThrow(new RuntimeException("down"));

        assertThatThrownBy(() -> ticketService.createTicket(new CreateTicketRequest("Subject", "Description"), 7L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("AI prediction service is unavailable");
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void createRequestDoesNotExposeRoutingInputs() {
        assertThat(CreateTicketRequest.class.getRecordComponents())
                .extracting(component -> component.getName())
                .containsExactly("subject", "description");
    }

    private void givenValidCustomer() {
        User user = new User();
        user.setId(7L);
        user.setUsername("customer");
        user.setFullName("Customer");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(Status.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
    }

    private void givenValidMasterData(String categoryCode, String priorityCode) {
        TicketCategory category = new TicketCategory();
        category.setId(11L);
        category.setCode(categoryCode);
        category.setName("Domain category");
        category.setStatus(Status.ACTIVE);
        category.setCreatedAt(LocalDateTime.now());
        when(ticketCategoryRepository.findByCodeAndStatus(categoryCode, Status.ACTIVE)).thenReturn(Optional.of(category));
        when(ticketCategoryRepository.findById(11L)).thenReturn(Optional.of(category));

        TicketPriorityMaster priority = new TicketPriorityMaster();
        priority.setId(3L);
        priority.setCode(priorityCode);
        priority.setName("High");
        priority.setStatus(Status.ACTIVE);
        priority.setCreatedAt(LocalDateTime.now());
        when(priorityRepository.findByCodeAndStatus(priorityCode, Status.ACTIVE)).thenReturn(Optional.of(priority));

        TicketCategoryDepartmentMapping mapping = new TicketCategoryDepartmentMapping();
        mapping.setId(33L);
        mapping.setCategoryId(11L);
        mapping.setDepartmentId(22L);
        mapping.setStatus(Status.ACTIVE);
        mapping.setCreatedAt(LocalDateTime.now());
        when(mappingRepository.findByCategoryIdAndStatus(11L, Status.ACTIVE)).thenReturn(Optional.of(mapping));

        Department department = new Department();
        department.setId(22L);
        department.setCode("GENERAL_SUPPORT");
        department.setName("General Support");
        department.setVendorId(1L);
        department.setStatus(Status.ACTIVE);
        department.setCreatedAt(LocalDateTime.now());
        when(departmentRepository.findById(22L)).thenReturn(Optional.of(department));
    }

    private AiPredictionResponse prediction(String category,
                                            Double categoryConfidence,
                                            String priority,
                                            Double priorityConfidence,
                                            Boolean requiresManualReview) {
        return new AiPredictionResponse(category, categoryConfidence, priority, priorityConfidence, requiresManualReview);
    }
}
