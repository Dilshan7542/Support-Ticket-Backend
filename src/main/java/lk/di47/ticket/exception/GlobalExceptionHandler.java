package lk.di47.ticket.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lk.di47.ticket.constant.endpoint.SecurityEndpoint;
import lk.di47.ticket.response.ApiErrorResponseWriter;
import lk.di47.ticket.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Log4j2
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ApiErrorResponseWriter apiErrorResponseWriter;

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NotFoundException exception) {
        return apiErrorResponseWriter.toResponseEntity(exception);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusiness(BusinessException exception) {
        return apiErrorResponseWriter.toResponseEntity(exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException exception,
                                                                HttpServletRequest request) {
        log.error("Validation exception -> {}", exception.getMessage(), exception);
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        Map<String, Object> data = apiErrorResponseWriter.displayableErrorData(
                ErrorCode.INVALID_REQUEST,
                "Validation failed",
                errors
        );
        if (SecurityEndpoint.KEY_EXCHANGE.equals(request.getRequestURI())) {
            return ResponseEntity.badRequest().body(ApiResponse.failed("Validation failed", data));
        }
        return ResponseEntity.ok(ApiResponse.failed("Validation failed", data));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException exception,
                                                                         HttpServletRequest request) {
        log.error("Constraint violation exception -> {}", exception.getMessage(), exception);
        Map<String, Object> data = apiErrorResponseWriter.displayableErrorData(
                ErrorCode.INVALID_REQUEST,
                exception.getMessage(),
                null
        );
        if (SecurityEndpoint.KEY_EXCHANGE.equals(request.getRequestURI())) {
            return ResponseEntity.badRequest().body(ApiResponse.failed(exception.getMessage(), data));
        }
        return ResponseEntity.ok(ApiResponse.failed(exception.getMessage(), data));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        log.error("Unhandled exception -> {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failed(
                        ErrorCode.INTERNAL_ERROR.getDefaultMessage(),
                        apiErrorResponseWriter.errorData(ErrorCode.INTERNAL_ERROR)
                ));
    }
}
