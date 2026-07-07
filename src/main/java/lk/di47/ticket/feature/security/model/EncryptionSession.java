package lk.di47.ticket.feature.security.model;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EncryptionSession {
    private static final int MAX_TRACKED_NONCES = 2_000;

    private final String keyId;
    private final SecretKey encryptionKey;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final Map<String, Instant> usedNonces = new ConcurrentHashMap<>();

    public EncryptionSession(String keyId, SecretKey encryptionKey, Instant createdAt, Instant expiresAt) {
        this.keyId = keyId;
        this.encryptionKey = encryptionKey;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getKeyId() {
        return keyId;
    }

    public SecretKey getEncryptionKey() {
        return encryptionKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public boolean registerNonce(String nonce, Instant now, Duration retention) {
        if (usedNonces.size() >= MAX_TRACKED_NONCES) {
            Instant threshold = now.minus(retention);
            usedNonces.entrySet().removeIf(entry -> entry.getValue().isBefore(threshold));
        }
        return usedNonces.putIfAbsent(nonce, now) == null;
    }
}
