package lk.di47.ticket.feature.dashboard.dto;

import lk.di47.ticket.util.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRecentTicketResponse {
    private Long ticketId;
    private String ticketNo;
    private String subject;
    private String categoryName;
    private String categoryCode;
    private String priority;
    private TicketStatus status;
    private Long departmentId;
    private String departmentName;
    private Long assignedStaffId;
    private String assignedStaffName;
    private LocalDateTime createdAt;
}
