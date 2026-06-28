package lk.di47.ticket.feature.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardCardMetricsResponse {
    private Long totalTickets;
    private Long openTickets;
    private Long resolvedTickets;
    private Long closedTickets;
    private Long criticalTickets;
    private Long highPriorityTickets;
    private Long unassignedTickets;
    private Long todayTickets;
    private Long activeUsers;
    private Long activeDepartments;
    private Long unreadNotifications;
    private Long failedEmails;
    private Long aiPredictions;
}
