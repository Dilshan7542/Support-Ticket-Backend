package lk.di47.ticket.feature.dashboard.service;

import lk.di47.ticket.feature.dashboard.dto.DashboardRequest;
import lk.di47.ticket.feature.dashboard.dto.DashboardSummaryResponse;

public interface DashboardService {
    DashboardSummaryResponse getSummary(DashboardRequest request);
}
