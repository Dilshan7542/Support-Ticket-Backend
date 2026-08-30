package lk.di47.ticket.feature.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRecentActivityResponse {
    private Long id;
    private String traceId;
    private Long userId;
    private String action;
    private String endpoint;
    private Integer responseStatus;
    private Long executionTimeMs;
    private LocalDateTime createdAt;
}
