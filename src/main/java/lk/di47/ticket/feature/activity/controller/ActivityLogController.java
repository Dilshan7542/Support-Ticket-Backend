package lk.di47.ticket.feature.activity.controller;

import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.endpoint.ActivityEndpoint;
import lk.di47.ticket.feature.activity.dto.ActivityLogDetailRequest;
import lk.di47.ticket.feature.activity.dto.ActivityLogListRequest;
import lk.di47.ticket.feature.activity.dto.ActivityLogResponse;
import lk.di47.ticket.feature.activity.service.ActivityLogService;
import lk.di47.ticket.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ActivityLogController {
    private final ActivityLogService activityLogService;

    @PostMapping(ActivityEndpoint.LIST)
    public ApiResponse<List<ActivityLogResponse>> list(@Valid @RequestBody ActivityLogListRequest request) {
        return ApiResponse.success(MessageConstant.SUCCESS, activityLogService.list());
    }

    @PostMapping(ActivityEndpoint.DETAIL)
    public ApiResponse<ActivityLogResponse> detail(@Valid @RequestBody ActivityLogDetailRequest request) {
        return ApiResponse.success(MessageConstant.SUCCESS, activityLogService.detail(request));
    }
}
