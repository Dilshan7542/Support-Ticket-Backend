package lk.di47.ticket.feature.activity.service.impl;

import lk.di47.ticket.entity.ActivityLog;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.activity.dto.ActivityLogDetailRequest;
import lk.di47.ticket.feature.activity.dto.ActivityLogResponse;
import lk.di47.ticket.feature.activity.service.ActivityLogService;
import lk.di47.ticket.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {
    private final ActivityLogRepository activityLogRepository;

    @Override
    public List<ActivityLogResponse> list() {
        return activityLogRepository.findTop100ByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Override
    public ActivityLogResponse detail(ActivityLogDetailRequest request) {
        return activityLogRepository.findById(request.activityLogId())
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Activity log not found"));
    }

    private ActivityLogResponse toResponse(ActivityLog log) {
        return new ActivityLogResponse(
                log.getId(),
                log.getTraceId(),
                log.getUserId(),
                log.getHttpMethod(),
                log.getEndpoint(),
                log.getAction(),
                log.getResponseStatus(),
                log.isEncryptionEnabled(),
                log.getExecutionTimeMs(),
                log.getCreatedAt()
        );
    }
}
