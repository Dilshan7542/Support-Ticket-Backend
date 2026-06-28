package lk.di47.ticket.feature.auth.dto;

public record RefreshTokenResponse(
        Long userId,
        String accessToken,
        String refreshToken,
        String encryptionKeyId
) {
}
