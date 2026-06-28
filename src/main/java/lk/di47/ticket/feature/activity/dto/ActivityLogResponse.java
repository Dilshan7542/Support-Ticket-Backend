package lk.di47.ticket.feature.activity.dto;

import java.time.LocalDateTime;

public record ActivityLogResponse(
        Long id,
        String traceId,
        Long userId,
        String httpMethod,
        String endpoint,
        String action,
        Integer responseStatus,
        boolean encryptionEnabled,
        Long executionTimeMs,
        LocalDateTime createdAt
) {
}
