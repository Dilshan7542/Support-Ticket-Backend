package lk.di47.ticket.feature.dashboard.service.impl;

import lk.di47.ticket.entity.ActivityLog;
import lk.di47.ticket.entity.Department;
import lk.di47.ticket.entity.Ticket;
import lk.di47.ticket.entity.TicketCategory;
import lk.di47.ticket.feature.dashboard.dto.*;
import lk.di47.ticket.feature.dashboard.service.DashboardService;
import lk.di47.ticket.repository.*;
import lk.di47.ticket.util.enums.MailStatus;
import lk.di47.ticket.util.enums.Status;
import lk.di47.ticket.util.enums.TicketPriority;
import lk.di47.ticket.util.enums.TicketStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private static final int DEFAULT_DASHBOARD_DAYS = 7;

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationRepository notificationRepository;
    private final MailLogRepository mailLogRepository;
    private final AiPredictionRepository aiPredictionRepository;
    private final ActivityLogRepository activityLogRepository;
    private final TicketCategoryRepository ticketCategoryRepository;

    @Override
    public DashboardSummaryResponse getSummary(DashboardRequest request) {
        LocalDate dateTo = request.getDateTo() == null ? LocalDate.now() : request.getDateTo();
        LocalDate dateFrom = request.getDateFrom() == null ? dateTo.minusDays(DEFAULT_DASHBOARD_DAYS - 1L) : request.getDateFrom();
        LocalDateTime startDateTime = dateFrom.atStartOfDay();
        LocalDateTime endDateTime = dateTo.plusDays(1).atStartOfDay().minusNanos(1);

        List<Ticket> periodTickets = ticketRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        List<Ticket> recentTickets = ticketRepository.findTop10ByOrderByCreatedAtDesc();
        Map<Long, String> departmentNames = departmentRepository.findAll().stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));
        Map<String, String> categoryNames = ticketCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(TicketCategory::getCode, TicketCategory::getName));

        return DashboardSummaryResponse.builder()
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .cardMetrics(buildCardMetrics())
                .ticketsByStatus(buildStatusMetrics())
                .ticketsByPriority(buildPriorityMetrics())
                .ticketsByCategory(buildCategoryMetrics(periodTickets, categoryNames))
                .ticketsByDepartment(buildDepartmentMetrics(periodTickets, departmentNames))
                .dailyTicketTrend(buildDailyTrend(dateFrom, dateTo, periodTickets))
                .recentTickets(buildRecentTickets(recentTickets, departmentNames, categoryNames))
                .recentActivities(buildRecentActivities())
                .build();
    }

    private DashboardCardMetricsResponse buildCardMetrics() {
        long totalTickets = ticketRepository.count();
        long resolvedTickets = ticketRepository.countByStatus(TicketStatus.RESOLVED);
        long closedTickets = ticketRepository.countByStatus(TicketStatus.CLOSED);
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().plusDays(1).atStartOfDay().minusNanos(1);

        return DashboardCardMetricsResponse.builder()
                .totalTickets(totalTickets)
                .openTickets(totalTickets - resolvedTickets - closedTickets)
                .resolvedTickets(resolvedTickets)
                .closedTickets(closedTickets)
                .criticalTickets(ticketRepository.countByPriority(TicketPriority.CRITICAL))
                .highPriorityTickets(ticketRepository.countByPriority(TicketPriority.HIGH))
                .unassignedTickets(ticketRepository.countByDepartmentIdIsNull())
                .todayTickets(ticketRepository.countByCreatedAtBetween(todayStart, todayEnd))
                .activeUsers(userRepository.countByStatus(Status.ACTIVE))
                .activeDepartments(departmentRepository.countByStatus(Status.ACTIVE))
                .unreadNotifications(notificationRepository.countByReadStatus(false))
                .failedEmails(mailLogRepository.countByStatus(MailStatus.FAILED))
                .aiPredictions(aiPredictionRepository.count())
                .build();
    }

    private List<DashboardMetricResponse> buildStatusMetrics() {
        Map<TicketStatus, Long> metrics = new EnumMap<>(TicketStatus.class);
        for (TicketStatus status : TicketStatus.values()) {
            metrics.put(status, ticketRepository.countByStatus(status));
        }
        return metrics.entrySet().stream()
                .map(entry -> DashboardMetricResponse.builder()
                        .label(entry.getKey().name())
                        .value(entry.getValue())
                        .build())
                .toList();
    }

    private List<DashboardMetricResponse> buildPriorityMetrics() {
        Map<TicketPriority, Long> metrics = new EnumMap<>(TicketPriority.class);
        for (TicketPriority priority : TicketPriority.values()) {
            metrics.put(priority, ticketRepository.countByPriority(priority));
        }
        return metrics.entrySet().stream()
                .map(entry -> DashboardMetricResponse.builder()
                        .label(entry.getKey().name())
                        .value(entry.getValue())
                        .build())
                .toList();
    }

    private List<DashboardMetricResponse> buildCategoryMetrics(List<Ticket> tickets, Map<String, String> categoryNames) {
        Map<String, Long> metrics = tickets.stream()
                .map(ticket -> resolveCategoryName(ticket.getCategoryCode(), categoryNames))
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
        return toSortedMetricList(metrics);
    }

    private List<DashboardMetricResponse> buildDepartmentMetrics(List<Ticket> tickets, Map<Long, String> departmentNames) {
        Map<String, Long> metrics = tickets.stream()
                .map(ticket -> resolveDepartmentName(ticket.getDepartmentId(), departmentNames))
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
        return toSortedMetricList(metrics);
    }

    private List<DashboardDailyTicketTrendResponse> buildDailyTrend(LocalDate dateFrom, LocalDate dateTo, List<Ticket> tickets) {
        Map<LocalDate, Long> countsByDate = tickets.stream()
                .collect(Collectors.groupingBy(ticket -> ticket.getCreatedAt().toLocalDate(), Collectors.counting()));

        List<DashboardDailyTicketTrendResponse> trend = new ArrayList<>();
        for (LocalDate date = dateFrom; !date.isAfter(dateTo); date = date.plusDays(1)) {
            trend.add(DashboardDailyTicketTrendResponse.builder()
                    .date(date)
                    .ticketCount(countsByDate.getOrDefault(date, 0L))
                    .build());
        }
        return trend;
    }

    private List<DashboardRecentTicketResponse> buildRecentTickets(List<Ticket> tickets,
                                                                   Map<Long, String> departmentNames,
                                                                   Map<String, String> categoryNames) {
        return tickets.stream()
                .map(ticket -> DashboardRecentTicketResponse.builder()
                        .ticketId(ticket.getId())
                        .ticketNo(ticket.getTicketNo())
                        .subject(ticket.getSubject())
                        .categoryName(resolveCategoryName(ticket.getCategoryCode(), categoryNames))
                        .categoryCode(ticket.getCategoryCode())
                        .priority(ticket.getPriority())
                        .status(ticket.getStatus())
                        .departmentId(ticket.getDepartmentId())
                        .departmentName(resolveDepartmentName(ticket.getDepartmentId(), departmentNames))
                        .assignedStaffId(ticket.getAssignedStaffId())
                        .createdAt(ticket.getCreatedAt())
                        .build())
                .toList();
    }

    private List<DashboardRecentActivityResponse> buildRecentActivities() {
        return activityLogRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(this::toActivityResponse)
                .toList();
    }

    private DashboardRecentActivityResponse toActivityResponse(ActivityLog activityLog) {
        return DashboardRecentActivityResponse.builder()
                .id(activityLog.getId())
                .traceId(activityLog.getTraceId())
                .userId(activityLog.getUserId())
                .action(activityLog.getAction())
                .endpoint(activityLog.getEndpoint())
                .responseStatus(activityLog.getResponseStatus())
                .executionTimeMs(activityLog.getExecutionTimeMs())
                .createdAt(activityLog.getCreatedAt())
                .build();
    }

    private String resolveDepartmentName(Long departmentId, Map<Long, String> departmentNames) {
        if (departmentId == null) {
            return "UNASSIGNED";
        }
        return departmentNames.getOrDefault(departmentId, "UNKNOWN");
    }

    private String resolveCategoryName(String categoryCode, Map<String, String> categoryNames) {
        if (categoryCode == null || categoryCode.isBlank()) {
            return "UNCATEGORIZED";
        }
        return categoryNames.getOrDefault(categoryCode, categoryCode);
    }

    private List<DashboardMetricResponse> toSortedMetricList(Map<String, Long> metrics) {
        return metrics.entrySet().stream()
                .filter(entry -> Objects.nonNull(entry.getKey()))
                .sorted((first, second) -> Long.compare(second.getValue(), first.getValue()))
                .map(entry -> DashboardMetricResponse.builder()
                        .label(entry.getKey())
                        .value(entry.getValue())
                        .build())
                .toList();
    }
}
