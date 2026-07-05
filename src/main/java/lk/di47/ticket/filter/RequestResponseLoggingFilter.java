package lk.di47.ticket.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.di47.ticket.constant.CryptoConstant;
import lk.di47.ticket.constant.SecurityConstant;
import lk.di47.ticket.constant.SecurityPathConstant;
import lk.di47.ticket.crypto.CryptoProperties;
import lk.di47.ticket.entity.ActivityLog;
import lk.di47.ticket.repository.ActivityLogRepository;
import lk.di47.ticket.util.generator.ActivityActionGenerator;
import lk.di47.ticket.util.mask.SensitiveDataMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

@Log4j2
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@RequiredArgsConstructor
public class RequestResponseLoggingFilter extends OncePerRequestFilter {
    private static final int MAX_LOG_BODY_LENGTH = 4000;

    private final ActivityLogRepository activityLogRepository;
    private final CryptoProperties cryptoProperties;
    private final JsonMapper jsonMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return Arrays.stream(SecurityPathConstant.LOGGING_EXCLUDED_PATHS)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request,1024 * 1024);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Exception exception) {
            String requestBody = isMultipartRequest(request) ? null : resolveRequestBody(request, wrappedRequest);
            log.error("Request filter chain failed for {} {} with body {} -> {}",
                    request.getMethod(),
                    getRequestUrl(request),
                    SensitiveDataMasker.mask(requestBody),
                    exception.getMessage(),
                    exception);
            throw exception;
        } finally {
            long executionTimeMs = System.currentTimeMillis() - start;
            String requestBody = isMultipartRequest(request) ? null : resolveRequestBody(request, wrappedRequest);
            String responseBody = resolveResponseBody(request, wrappedResponse);
            String maskedRequestBody = SensitiveDataMasker.mask(requestBody);
            String maskedResponseBody = SensitiveDataMasker.mask(responseBody);

            saveActivityLog(request, maskedRequestBody, maskedResponseBody, requestBody, wrappedResponse, executionTimeMs);
            logRequestResponse(request, wrappedResponse, executionTimeMs, maskedRequestBody, maskedResponseBody);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void saveActivityLog(HttpServletRequest originalRequest,
                                 String maskedRequestBody,
                                 String maskedResponseBody,
                                 String requestBody,
                                 ContentCachingResponseWrapper response,
                                 long executionTimeMs) {
        try {
            ActivityLog log = new ActivityLog();
            log.setTraceId((String) originalRequest.getAttribute(CryptoConstant.TRACE_ID));
            log.setUserId(resolveUserId(originalRequest, requestBody));
            log.setHttpMethod(originalRequest.getMethod());
            log.setEndpoint(originalRequest.getRequestURI());
            log.setAction(ActivityActionGenerator.generate(originalRequest.getRequestURI()));
            log.setRequestBody(resolveActivityLogBody(maskedRequestBody));
            log.setResponseBody(resolveActivityLogBody(maskedResponseBody));
            log.setResponseStatus(response.getStatus());
            log.setIpAddress(originalRequest.getRemoteAddr());
            log.setUserAgent(originalRequest.getHeader("User-Agent"));
            log.setEncryptionEnabled(cryptoProperties.isEnabled());
            log.setExecutionTimeMs(executionTimeMs);
            log.setCreatedAt(LocalDateTime.now());
            activityLogRepository.save(log);
        } catch (Exception exception) {
            log.error("Activity log save failed -> {}", exception.getMessage(), exception);
        }
    }

    private void logRequestResponse(HttpServletRequest request,
                                    ContentCachingResponseWrapper response,
                                    long executionTimeMs,
                                    String maskedRequestBody,
                                    String maskedResponseBody) {
        log.info("{} {} completed with status {} in {} ms",
                request.getMethod(), request.getRequestURI(), response.getStatus(), executionTimeMs);

        if (!log.isDebugEnabled()) {
            return;
        }

        log.debug("\n\n\nURL -> {} {}", request.getMethod(), getRequestUrl(request));
        log.debug("Headers -> [{}]", formatHeaders(request));
        log.debug("Request Body -> {}", formatJsonForLog(maskedRequestBody));
        log.debug("Response Status -> {}", response.getStatus());
        log.debug("Execution Time -> {} ms", executionTimeMs);
        log.debug("Response Body -> {}", formatJsonForLog(maskedResponseBody));
    }

    private String resolveRequestBody(HttpServletRequest originalRequest, ContentCachingRequestWrapper request) {
        String requestBody = (String) originalRequest.getAttribute(CryptoConstant.DECRYPTED_REQUEST_BODY);
        if (requestBody != null) {
            return requestBody;
        }
        return new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
    }

    private String resolveResponseBody(HttpServletRequest originalRequest, ContentCachingResponseWrapper response) {
        if (shouldSkipResponseBody(response)) {
            return null;
        }
        String responseBody = (String) originalRequest.getAttribute(CryptoConstant.PLAIN_RESPONSE_BODY);
        if (responseBody != null) {
            return responseBody;
        }
        return new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
    }

    private boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase(java.util.Locale.ROOT).startsWith("multipart/");
    }

    private boolean shouldSkipResponseBody(ContentCachingResponseWrapper response) {
        String contentDisposition = response.getHeader("Content-Disposition");
        if (contentDisposition != null && contentDisposition.toLowerCase(java.util.Locale.ROOT).contains("attachment")) {
            return true;
        }

        String contentType = response.getContentType();
        if (contentType == null || contentType.isBlank()) {
            return false;
        }
        String lowerContentType = contentType.toLowerCase(java.util.Locale.ROOT);
        return !lowerContentType.startsWith("text/")
                && !lowerContentType.contains("json")
                && !lowerContentType.contains("xml");
    }

    private String truncateForLog(String body) {
        if (body == null || body.length() <= MAX_LOG_BODY_LENGTH) {
            return body;
        }
        return body.substring(0, MAX_LOG_BODY_LENGTH);
    }

    private String getRequestUrl(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isBlank()) {
            return request.getRequestURI();
        }
        return request.getRequestURL() + "?" + queryString;
    }

    private String formatHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .map(header -> header + ": " + maskHeaderValue(header, request.getHeader(header)))
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private String maskHeaderValue(String header, String value) {
        if (value == null) {
            return null;
        }
        if ("authorization".equalsIgnoreCase(header)) {
            return "****";
        }
        return value;
    }

    private String formatJsonForLog(String body) {
        String truncatedBody = truncateForLog(body);
        if (truncatedBody == null || truncatedBody.isBlank()) {
            return "";
        }
        try {
            return jsonMapper.writeValueAsString(jsonMapper.readTree(truncatedBody));
        } catch (Exception exception) {
            log.error("Unable to format JSON for request/response log -> {}", exception.getMessage(), exception);
            return truncatedBody;
        }
    }

    private String resolveActivityLogBody(String body) {
        if (isEncryptedPayload(body)) {
            return null;
        }
        return body;
    }

    private boolean isEncryptedPayload(String body) {
        if (body == null || body.isBlank()) {
            return false;
        }
        try {
            JsonNode node = jsonMapper.readTree(body);
            return node != null
                    && node.isObject()
                    && node.get("iv") != null
                    && node.get("cipherText") != null;
        } catch (Exception exception) {
            log.error("Unable to inspect encrypted payload shape -> {}", exception.getMessage(), exception);
            return false;
        }
    }

    private Long resolveUserId(HttpServletRequest request, String requestBody) {
        Object userId = request.getAttribute(SecurityConstant.CURRENT_USER_ID);
        if (userId instanceof Long value) {
            return value;
        }
        try {
            if (requestBody == null || requestBody.isBlank()) {
                return null;
            }
            JsonNode node = jsonMapper.readTree(requestBody).get("userId");
            return node == null || node.isNull() ? null : node.asLong();
        } catch (Exception exception) {
            log.error("Unable to resolve user id from request body -> {}", exception.getMessage(), exception);
            return null;
        }
    }
}
