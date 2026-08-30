package lk.di47.ticket.feature.security.service.impl;

import jakarta.annotation.PostConstruct;
import lk.di47.ticket.crypto.CryptoProperties;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.feature.security.dto.KeyExchangeRequest;
import lk.di47.ticket.feature.security.dto.KeyExchangeResponse;
import lk.di47.ticket.feature.security.model.EncryptionSession;
import lk.di47.ticket.feature.security.service.KeyExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
public class KeyExchangeServiceImpl implements KeyExchangeService {
    private static final String EC_ALGORITHM = "EC";
    private static final String ECDH_ALGORITHM = "ECDH";
    private static final String CURVE = "secp256r1";
    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final String AES_ALGORITHM = "AES";
    private static final byte[] HKDF_INFO = "support-ticket-backend:browser-session:v1".getBytes(StandardCharsets.UTF_8);
    private static final int AES_256_KEY_BYTES = 32;
    private static final int SALT_LENGTH_BYTES = 32;
    private static final String DEV_FIXED_KEY_ID = "b37acbd1-1587-4ae9-b7cf-e2ccb62967c8";

    private final CryptoProperties cryptoProperties;
    private final ConcurrentMap<String, EncryptionSession> sessions = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @PostConstruct
    public void init() {
        if (isProdProfile()) {
            return;
        }

        byte[] keyBytes = cryptoProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != AES_256_KEY_BYTES) {
            throw new IllegalStateException("app.crypto.secret-key must be exactly 32 bytes for the dev fixed key");
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(cryptoProperties.getSessionTtlMinutes()));
        SecretKey encryptionKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);
        sessions.put(DEV_FIXED_KEY_ID, new EncryptionSession(DEV_FIXED_KEY_ID, encryptionKey, now, expiresAt));
    }
    private boolean isProdProfile() {
        return "prod".equals(this.activeProfile);
    }

    @Override
    public KeyExchangeResponse createExchange(KeyExchangeRequest request) {
        try {
            removeExpiredSessions();

            PublicKey clientPublicKey = decodeClientPublicKey(request.clientPublicKey());
            KeyPair serverKeyPair = generateServerKeyPair();
            byte[] sharedSecret = deriveSharedSecret(serverKeyPair, clientPublicKey);

            byte[] salt = new byte[SALT_LENGTH_BYTES];
            secureRandom.nextBytes(salt);
            SecretKey encryptionKey = new SecretKeySpec(
                    deriveHkdfKey(sharedSecret, salt, HKDF_INFO, AES_256_KEY_BYTES),
                    AES_ALGORITHM
            );

            Instant now = Instant.now();
            Instant expiresAt = now.plus(Duration.ofMinutes(cryptoProperties.getSessionTtlMinutes()));
            String keyId = UUID.randomUUID().toString();
            sessions.put(keyId, new EncryptionSession(keyId, encryptionKey, now, expiresAt));

            return new KeyExchangeResponse(
                    keyId,
                    Base64.getEncoder().encodeToString(serverKeyPair.getPublic().getEncoded()),
                    Base64.getEncoder().encodeToString(salt),
                    "P-256",
                    "HKDF-SHA-256",
                    "AES-256-GCM",
                    expiresAt
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.KEY_EXCHANGE_FAILED, "Unable to complete key exchange", exception);
        }
    }

    @Override
    public SecretKey resolveEncryptionKey(String keyId) {
        return getActiveSession(keyId).getEncryptionKey();
    }

    @Override
    public void validateTimestamp(String keyId, String timestamp) {
        getActiveSession(keyId);
        try {
            Instant requestTime = Instant.parse(timestamp);
            Duration allowedDifference = Duration.ofSeconds(cryptoProperties.getTimestampToleranceSeconds());
            if (Duration.between(requestTime, Instant.now()).abs().compareTo(allowedDifference) > 0) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "Expired request timestamp");
            }
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.CRYPTO_INVALID_REQUEST, "Invalid request timestamp", exception);
        }
    }

    @Override
    public void registerNonce(String keyId, String nonce) {
        if (nonce == null || nonce.isBlank() || nonce.length() > 200) {
            throw new BusinessException(ErrorCode.CRYPTO_INVALID_REQUEST, "Invalid request nonce");
        }

        EncryptionSession session = getActiveSession(keyId);
        boolean accepted = session.registerNonce(
                nonce,
                Instant.now(),
                Duration.ofSeconds(cryptoProperties.getTimestampToleranceSeconds())
        );
        if (!accepted) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Duplicate request detected");
        }
    }

    @Override
    public void validateSession(String keyId) {
        getActiveSession(keyId);
    }

    @Override
    public void invalidate(String keyId) {
        if (keyId != null) {
            sessions.remove(keyId);
        }
    }

    @Override
    public boolean isActive(String keyId) {
        try {
            getActiveSession(keyId);
            return true;
        } catch (BusinessException exception) {
            return false;
        }
    }

    private EncryptionSession getActiveSession(String keyId) {
        if (keyId == null || keyId.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Missing encryption key id");
        }

        EncryptionSession session = sessions.get(keyId);
        if (session == null) {
            throw new BusinessException(ErrorCode.ENCRYPTION_SESSION_EXPIRED, "Encryption session expired. Renew key exchange.");
        }
        if (session.isExpired(Instant.now())) {
            sessions.remove(keyId, session);
            throw new BusinessException(ErrorCode.ENCRYPTION_SESSION_EXPIRED, "Encryption session expired. Renew key exchange.");
        }
        return session;
    }

    private void removeExpiredSessions() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }



    private PublicKey decodeClientPublicKey(String encodedKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
        return KeyFactory.getInstance(EC_ALGORITHM).generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    private KeyPair generateServerKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(EC_ALGORITHM);
        generator.initialize(new ECGenParameterSpec(CURVE), secureRandom);
        return generator.generateKeyPair();
    }

    private byte[] deriveSharedSecret(KeyPair serverKeyPair, PublicKey clientPublicKey) throws Exception {
        KeyAgreement keyAgreement = KeyAgreement.getInstance(ECDH_ALGORITHM);
        keyAgreement.init(serverKeyPair.getPrivate());
        keyAgreement.doPhase(clientPublicKey, true);
        return keyAgreement.generateSecret();
    }

    private byte[] deriveHkdfKey(byte[] inputKeyMaterial, byte[] salt, byte[] info, int outputLength) throws Exception {
        Mac extractMac = Mac.getInstance(HMAC_SHA_256);
        extractMac.init(new SecretKeySpec(salt, HMAC_SHA_256));
        byte[] pseudoRandomKey = extractMac.doFinal(inputKeyMaterial);

        Mac expandMac = Mac.getInstance(HMAC_SHA_256);
        expandMac.init(new SecretKeySpec(pseudoRandomKey, HMAC_SHA_256));
        byte[] previousBlock = new byte[0];
        byte[] output = new byte[outputLength];
        int offset = 0;
        int counter = 1;

        while (offset < outputLength) {
            expandMac.reset();
            expandMac.update(previousBlock);
            expandMac.update(info);
            expandMac.update((byte) counter);
            previousBlock = expandMac.doFinal();

            int copyLength = Math.min(previousBlock.length, outputLength - offset);
            System.arraycopy(previousBlock, 0, output, offset, copyLength);
            offset += copyLength;
            counter++;
        }
        return output;
    }
}
