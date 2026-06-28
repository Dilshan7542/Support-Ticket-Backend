package lk.di47.ticket.security;

public record ValidatedUserContext(
        Long userId,
        String username,
        String role,
        String sessionId,
        String encryptionKeyId
) {
}
