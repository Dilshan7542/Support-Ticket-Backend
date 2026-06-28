package lk.di47.ticket.feature.auth.dto;

import jakarta.validation.constraints.NotNull;

public record LogoutRequest(
        @NotNull(message = "userId is required")
        Long userId
) {
}
