package lk.di47.ticket.crypto;

import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.feature.security.service.KeyExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SessionCryptoService {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;

    private final KeyExchangeService keyExchangeService;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptedPayload encrypt(String plainText, String keyId, String aad) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, resolveKey(keyId), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            return new EncryptedPayload(
                    Base64.getEncoder().encodeToString(iv),
                    Base64.getEncoder().encodeToString(cipherText)
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Unable to encrypt response", exception);
        }
    }

    public String decrypt(EncryptedPayload payload, String keyId, String aad) {
        try {
            byte[] iv = Base64.getDecoder().decode(payload.iv());
            if (iv.length != IV_LENGTH_BYTES) {
                throw new BusinessException(ErrorCode.CRYPTO_INVALID_REQUEST, "Invalid encryption IV");
            }

            byte[] cipherText = Base64.getDecoder().decode(payload.cipherText());
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, resolveKey(keyId), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.CRYPTO_INVALID_REQUEST, "Invalid encrypted payload", exception);
        }
    }

    private SecretKey resolveKey(String keyId) {
        return keyExchangeService.resolveEncryptionKey(keyId);
    }
}
