package lk.di47.ticket.feature.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private DashboardCardMetricsResponse cardMetrics;
    private List<DashboardMetricResponse> ticketsByStatus;
    private List<DashboardMetricResponse> ticketsByPriority;
    private List<DashboardMetricResponse> ticketsByCategory;
    private List<DashboardMetricResponse> ticketsByDepartment;
    private List<DashboardDailyTicketTrendResponse> dailyTicketTrend;
    private List<DashboardRecentTicketResponse> recentTickets;
    private List<DashboardRecentActivityResponse> recentActivities;
}
