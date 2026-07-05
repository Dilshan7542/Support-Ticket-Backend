package lk.di47.ticket.response;

import jakarta.servlet.http.HttpServletResponse;
import lk.di47.ticket.constant.SecurityConstant;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Log4j2
public class ApiErrorResponseWriter {
    private final JsonMapper jsonMapper;

    public ResponseEntity<ApiResponse<Object>> toResponseEntity(BusinessException exception) {
        logBusinessException(exception);
        HttpHeaders headers = new HttpHeaders();
        if (exception.getErrorCode() == ErrorCode.ENCRYPTION_SESSION_EXPIRED) {
            headers.set(SecurityConstant.CRYPTO_ACTION_HEADER, SecurityConstant.RENEW_KEY_EXCHANGE_ACTION);
        }
        return ResponseEntity
                .status(resolveStatus(exception.getErrorCode()))
                .headers(headers)
                .body(ApiResponse.failed(responseMessage(exception), errorData(exception)));
    }

    public void write(HttpServletResponse response, BusinessException exception) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        logBusinessException(exception);
        response.resetBuffer();
        response.setStatus(resolveStatus(exception.getErrorCode()).value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (exception.getErrorCode() == ErrorCode.ENCRYPTION_SESSION_EXPIRED) {
            response.setHeader(SecurityConstant.CRYPTO_ACTION_HEADER, SecurityConstant.RENEW_KEY_EXCHANGE_ACTION);
        }
        jsonMapper.writeValue(
                response.getOutputStream(),
                ApiResponse.failed(responseMessage(exception), errorData(exception))
        );
    }

    public void writeInternalServerError(HttpServletResponse response, String message, Throwable exception) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        log.error("Internal server error -> {}", message, exception);
        response.resetBuffer();
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getOutputStream(), ApiResponse.failed(
                ErrorCode.INTERNAL_ERROR.getDefaultMessage(),
                errorData(ErrorCode.INTERNAL_ERROR, message)
        ));
    }

    private HttpStatus resolveStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_REQUEST, NOT_FOUND -> HttpStatus.OK;
            case ENCRYPTION_SESSION_EXPIRED -> HttpStatus.CONFLICT;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case CRYPTO_INVALID_REQUEST, KEY_EXCHANGE_FAILED -> HttpStatus.BAD_REQUEST;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    public Map<String, Object> displayableErrorData(ErrorCode errorCode,
                                                    String displayMessage,
                                                    Map<String, ?> details) {
        Map<String, Object> data = errorData(errorCode, displayMessage);
        data.put("display", true);
        data.put("displayMessage", displayMessage);
        if (details != null && !details.isEmpty()) {
            data.put("details", details);
        }
        return data;
    }

    public Map<String, Object> errorData(ErrorCode errorCode) {
        return errorData(errorCode, null);
    }

    private Map<String, Object> errorData(BusinessException exception) {
        String displayMessage = exception.getErrorCode().isDisplay() ? exception.getMessage() : null;
        return errorData(exception.getErrorCode(), displayMessage);
    }

    private Map<String, Object> errorData(ErrorCode errorCode, String displayMessage) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", errorCode.name());
        data.put("display", errorCode.isDisplay());
        data.put("displayMessage", errorCode.isDisplay() ? displayMessage : null);
        data.put("severity", "ERROR");
        if (errorCode == ErrorCode.ENCRYPTION_SESSION_EXPIRED) {
            data.put("action", SecurityConstant.RENEW_KEY_EXCHANGE_ACTION);
        }
        return data;
    }

    private String responseMessage(BusinessException exception) {
        if (exception.getErrorCode().isDisplay()) {
            return exception.getMessage();
        }
        return exception.getErrorCode().getDefaultMessage();
    }

    private void logBusinessException(BusinessException exception) {
        log.error("BusinessException [{}] -> {}", exception.getErrorCode(), exception.getMessage(), exception);
    }
}
