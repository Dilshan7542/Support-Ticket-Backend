package lk.di47.ticket.feature.activity.service;

import lk.di47.ticket.feature.activity.dto.ActivityLogDetailRequest;
import lk.di47.ticket.feature.activity.dto.ActivityLogListRequest;
import lk.di47.ticket.feature.activity.dto.ActivityLogResponse;
import lk.di47.ticket.response.PageResponse;

public interface ActivityLogService {
    PageResponse<ActivityLogResponse> list(ActivityLogListRequest request);

    ActivityLogResponse detail(ActivityLogDetailRequest request);
}
