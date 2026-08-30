package lk.di47.ticket.feature.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDailyTicketTrendResponse {
    private LocalDate date;
    private Long ticketCount;
}
