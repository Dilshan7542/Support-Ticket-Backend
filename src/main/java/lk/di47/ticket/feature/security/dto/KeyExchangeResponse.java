package lk.di47.ticket.feature.security.dto;

import java.time.Instant;

public record KeyExchangeResponse(
        String keyId,
        String serverPublicKey,
        String salt,
        String curve,
        String keyDerivation,
        String encryptionAlgorithm,
        Instant expiresAt
) {
}
