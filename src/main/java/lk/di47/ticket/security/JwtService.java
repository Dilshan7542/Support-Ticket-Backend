package lk.di47.ticket.security;


import lk.di47.ticket.entity.User;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.util.enums.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final JwtProperties jwtProperties;
    private final JsonMapper jsonMapper;

    public String generateAccessToken(User user, String sessionId) {
        return generateToken(user, sessionId, TokenType.ACCESS, jwtProperties.getAccessTokenExpirationMinutes());
    }

    public String generateRefreshToken(User user, String sessionId) {
        return generateToken(user, sessionId, TokenType.REFRESH, jwtProperties.getRefreshTokenExpirationMinutes());
    }

    public JwtTokenData validate(String token, TokenType expectedType) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid token");
            }

            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = sign(unsignedToken);
            if (!MessageDigestTimingSafe.equals(expectedSignature, parts[2])) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid token signature");
            }

            String payloadJson = new String(BASE64_URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> payload = jsonMapper.readValue(
                    payloadJson,
                    new TypeReference<>() {
                    }
            );
            long expiresAt = Long.parseLong(String.valueOf(payload.get("exp")));
            if (Instant.now().getEpochSecond() > expiresAt) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "Token expired");
            }

            TokenType tokenType = TokenType.valueOf(String.valueOf(payload.get("typ")));
            if (tokenType != expectedType) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid token type");
            }

            return new JwtTokenData(
                    Long.valueOf(String.valueOf(payload.get("uid"))),
                    String.valueOf(payload.get("sub")),
                    String.valueOf(payload.get("role")),
                    String.valueOf(payload.get("sid")),
                    tokenType,
                    expiresAt
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid token", exception);
        }
    }

    private String generateToken(User user, String sessionId, TokenType tokenType, long expirationMinutes) {
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", user.getUsername());
            payload.put("uid", user.getId());
            payload.put("role", user.getRole().name());
            payload.put("sid", sessionId);
            payload.put("typ", tokenType.name());
            payload.put("iat", Instant.now().getEpochSecond());
            payload.put("exp", Instant.now().plusSeconds(expirationMinutes * 60).getEpochSecond());

            String encodedHeader = encodeJson(header);
            String encodedPayload = encodeJson(payload);
            String unsignedToken = encodedHeader + "." + encodedPayload;
            return unsignedToken + "." + sign(unsignedToken);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate token", exception);
        }
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        return BASE64_URL_ENCODER.encodeToString(jsonMapper.writeValueAsBytes(value));
    }

    private String sign(String unsignedToken) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
        return BASE64_URL_ENCODER.encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
    }

    private static final class MessageDigestTimingSafe {
        private MessageDigestTimingSafe() {
        }

        static boolean equals(String first, String second) {
            return java.security.MessageDigest.isEqual(
                    first.getBytes(StandardCharsets.UTF_8),
                    second.getBytes(StandardCharsets.UTF_8)
            );
        }
    }
}
