package lk.di47.ticket.feature.activity.service;

import lk.di47.ticket.feature.activity.dto.ActivityLogDetailRequest;
import lk.di47.ticket.feature.activity.dto.ActivityLogResponse;

import java.util.List;

public interface ActivityLogService {
    List<ActivityLogResponse> list();

    ActivityLogResponse detail(ActivityLogDetailRequest request);
}
