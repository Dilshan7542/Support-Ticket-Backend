package lk.di47.ticket.feature.dashboard.controller;

import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.endpoint.DashboardEndpoint;
import lk.di47.ticket.feature.dashboard.dto.DashboardRequest;
import lk.di47.ticket.feature.dashboard.dto.DashboardSummaryResponse;
import lk.di47.ticket.feature.dashboard.service.DashboardService;
import lk.di47.ticket.response.ApiResponse;
import lk.di47.ticket.util.mask.SensitiveDataMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequiredArgsConstructor
@Log4j2
public class DashboardController {
    private final DashboardService dashboardService;
    private final JsonMapper jsonMapper;

    @PostMapping(DashboardEndpoint.SUMMARY)
    public ApiResponse<DashboardSummaryResponse> summary(@Valid @RequestBody DashboardRequest request) {
        log.debug("Dashboard Summary -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, dashboardService.getSummary(request));
    }

    private String toJson(Object data) {
        return SensitiveDataMasker.mask(jsonMapper.writeValueAsString(data));
    }
}
