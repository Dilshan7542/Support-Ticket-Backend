package lk.di47.ticket.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.di47.ticket.constant.CryptoConstant;
import lk.di47.ticket.constant.SecurityConstant;
import lk.di47.ticket.crypto.CryptoProperties;
import lk.di47.ticket.crypto.EncryptedPayload;
import lk.di47.ticket.crypto.EncryptionAadFactory;
import lk.di47.ticket.crypto.SessionCryptoService;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.feature.security.service.KeyExchangeService;
import lk.di47.ticket.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class EncryptionFilter extends OncePerRequestFilter {
    private final CryptoProperties properties;
    private final KeyExchangeService keyExchangeService;
    private final SessionCryptoService sessionCryptoService;
    private final JsonMapper jsonMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.isEnabled() || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return properties.getExcludedPaths().stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String keyId = request.getHeader(SecurityConstant.KEY_ID_HEADER);
        String timestamp = request.getHeader(SecurityConstant.TIMESTAMP_HEADER);
        String nonce = request.getHeader(SecurityConstant.NONCE_HEADER);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            validateRequiredHeaders(keyId, timestamp, nonce);
            keyExchangeService.validateTimestamp(keyId, timestamp);

            String encryptedRequestBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            EncryptedPayload encryptedPayload = jsonMapper.readValue(encryptedRequestBody, EncryptedPayload.class);
            String requestAad = EncryptionAadFactory.requestAad(
                    keyId,
                    request.getMethod(),
                    request.getRequestURI(),
                    timestamp,
                    nonce
            );
            String decryptedRequest = sessionCryptoService.decrypt(encryptedPayload, keyId, requestAad);
            keyExchangeService.registerNonce(keyId, nonce);

            request.setAttribute(CryptoConstant.DECRYPTED_REQUEST_BODY, decryptedRequest);
            request.setAttribute(SecurityConstant.CURRENT_ENCRYPTION_KEY_ID, keyId);

            CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request, decryptedRequest);
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (BusinessException exception) {
            writeError(wrappedResponse, resolveStatus(exception), exception.getMessage());
        } catch (Exception exception) {
            writeError(wrappedResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to process encrypted request");
        }

        if (keyExchangeService.isActive(keyId)) {
            encryptAndWriteResponse(request, wrappedResponse, keyId, timestamp, nonce);
        } else {
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void validateRequiredHeaders(String keyId, String timestamp, String nonce) {
        if (keyId == null || keyId.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Missing X-Key-Id header");
        }
        if (timestamp == null || timestamp.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Missing X-Timestamp header");
        }
        if (nonce == null || nonce.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Missing X-Nonce header");
        }
    }

    private void encryptAndWriteResponse(HttpServletRequest request,
                                         ContentCachingResponseWrapper response,
                                         String keyId,
                                         String timestamp,
                                         String nonce) throws IOException {
        int responseStatus = response.getStatus();
        String plainResponse = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
        if (plainResponse.isBlank()) {
            plainResponse = jsonMapper.writeValueAsString(ApiResponse.success("Success", null));
        }

        request.setAttribute(CryptoConstant.PLAIN_RESPONSE_BODY, plainResponse);
        String responseAad = EncryptionAadFactory.responseAad(keyId, responseStatus, timestamp, nonce);
        EncryptedPayload encryptedResponse = sessionCryptoService.encrypt(plainResponse, keyId, responseAad);
        byte[] encryptedBytes = jsonMapper.writeValueAsBytes(encryptedResponse);

        response.resetBuffer();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(SecurityConstant.KEY_ID_HEADER, keyId);
        response.setHeader(SecurityConstant.ENCRYPTION_HEADER, "AES-256-GCM");
        response.getOutputStream().write(encryptedBytes);
        response.copyBodyToResponse();
    }

    private void writeError(ContentCachingResponseWrapper response, int status, String message) throws IOException {
        response.resetBuffer();
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getOutputStream(), ApiResponse.failed(message, null));
    }

    private int resolveStatus(BusinessException exception) {
        return exception.getErrorCode() == ErrorCode.UNAUTHORIZED
                ? HttpServletResponse.SC_UNAUTHORIZED
                : HttpServletResponse.SC_BAD_REQUEST;
    }
}
