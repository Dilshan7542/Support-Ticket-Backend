package lk.di47.ticket.response;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        boolean success,
        String statusCode,
        String message,
        T data,
        LocalDateTime timestamp
) {
    private static final String SUCCESS_CODE = "00";
    private static final String FAILED_CODE = "01";

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, SUCCESS_CODE, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> failed(String message, T data) {
        return new ApiResponse<>(false, FAILED_CODE, message, data, LocalDateTime.now());
    }
}
