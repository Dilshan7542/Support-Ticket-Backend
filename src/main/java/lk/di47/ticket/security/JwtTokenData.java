package lk.di47.ticket.security;


import lk.di47.ticket.util.enums.TokenType;

public record JwtTokenData(
        Long userId,
        String username,
        String role,
        String sessionId,
        TokenType tokenType,
        long expiresAt
) {
}
