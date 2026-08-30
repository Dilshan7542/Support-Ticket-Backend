package lk.di47.ticket.feature.auth.dto;

import lk.di47.ticket.util.enums.UserRole;

public record LoginResponse(
        Long userId,
        String username,
        String fullName,
        UserRole role,
        String accessToken,
        String refreshToken,
        String encryptionKeyId
) {
}
