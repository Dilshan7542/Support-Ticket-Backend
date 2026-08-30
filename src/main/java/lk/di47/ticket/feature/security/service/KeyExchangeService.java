package lk.di47.ticket.feature.security.service;

import lk.di47.ticket.feature.security.dto.KeyExchangeRequest;
import lk.di47.ticket.feature.security.dto.KeyExchangeResponse;

import javax.crypto.SecretKey;

public interface KeyExchangeService {
    KeyExchangeResponse createExchange(KeyExchangeRequest request);

    SecretKey resolveEncryptionKey(String keyId);

    void validateTimestamp(String keyId, String timestamp);

    void registerNonce(String keyId, String nonce);

    void validateSession(String keyId);

    void invalidate(String keyId);

    boolean isActive(String keyId);
}
