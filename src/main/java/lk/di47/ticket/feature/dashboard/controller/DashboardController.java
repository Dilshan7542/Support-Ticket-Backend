package lk.di47.ticket.feature.dashboard.controller;

import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.endpoint.DashboardEndpoint;
import lk.di47.ticket.feature.dashboard.dto.DashboardRequest;
import lk.di47.ticket.feature.dashboard.dto.DashboardSummaryResponse;
import lk.di47.ticket.feature.dashboard.service.DashboardService;
import lk.di47.ticket.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @PostMapping(DashboardEndpoint.SUMMARY)
    public ApiResponse<DashboardSummaryResponse> summary(@Valid @RequestBody DashboardRequest request) {
        return ApiResponse.success(MessageConstant.SUCCESS, dashboardService.getSummary(request));
    }
}
