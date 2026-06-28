package lk.di47.ticket.feature.activity.dto;

import jakarta.validation.constraints.NotNull;

public record ActivityLogDetailRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "activityLogId is required")
        Long activityLogId
) {
}
