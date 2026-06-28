package lk.di47.ticket.feature.dashboard.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DashboardRequest {
    @NotNull(message = "userId is required")
    private Long userId;

    private LocalDate dateFrom;
    private LocalDate dateTo;
}
