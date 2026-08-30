package lk.di47.ticket.feature.activity.dto;

import jakarta.validation.constraints.NotNull;

public record ActivityLogListRequest(
        @NotNull(message = "userId is required")
        Long userId,

        Integer page,
        Integer size
) {
}
