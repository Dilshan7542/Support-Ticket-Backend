package lk.di47.ticket.feature.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
