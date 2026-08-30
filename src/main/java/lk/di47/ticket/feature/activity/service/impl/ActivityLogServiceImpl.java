package lk.di47.ticket.feature.activity.service.impl;

import lk.di47.ticket.entity.ActivityLog;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.activity.dto.ActivityLogDetailRequest;
import lk.di47.ticket.feature.activity.dto.ActivityLogListRequest;
import lk.di47.ticket.feature.activity.dto.ActivityLogResponse;
import lk.di47.ticket.feature.activity.service.ActivityLogService;
import lk.di47.ticket.repository.ActivityLogRepository;
import lk.di47.ticket.response.PageResponse;
import lk.di47.ticket.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {
    private final ActivityLogRepository activityLogRepository;

    @Override
    public PageResponse<ActivityLogResponse> list(ActivityLogListRequest request) {
        return PageResponse.from(
                activityLogRepository.findAll(PaginationUtil.toPageable(
                        request.page(),
                        request.size(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )),
                this::toResponse
        );
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
