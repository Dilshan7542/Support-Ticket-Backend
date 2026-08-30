package lk.di47.ticket.feature.activity.controller;

import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.endpoint.ActivityEndpoint;
import lk.di47.ticket.feature.activity.dto.ActivityLogDetailRequest;
import lk.di47.ticket.feature.activity.dto.ActivityLogListRequest;
import lk.di47.ticket.feature.activity.dto.ActivityLogResponse;
import lk.di47.ticket.feature.activity.service.ActivityLogService;
import lk.di47.ticket.response.ApiResponse;
import lk.di47.ticket.response.PageResponse;
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
public class ActivityLogController {
    private final ActivityLogService activityLogService;
    private final JsonMapper jsonMapper;

    @PostMapping(ActivityEndpoint.LIST)
    public ApiResponse<PageResponse<ActivityLogResponse>> list(@Valid @RequestBody ActivityLogListRequest request) {
        log.debug("List Activity Log -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, activityLogService.list(request));
    }

    @PostMapping(ActivityEndpoint.DETAIL)
    public ApiResponse<ActivityLogResponse> detail(@Valid @RequestBody ActivityLogDetailRequest request) {
        log.debug("Detail Activity Log -> {}", this.toJson(request));
        return ApiResponse.success(MessageConstant.SUCCESS, activityLogService.detail(request));
    }

    private String toJson(Object data) {
        return SensitiveDataMasker.mask(jsonMapper.writeValueAsString(data));
    }
}
