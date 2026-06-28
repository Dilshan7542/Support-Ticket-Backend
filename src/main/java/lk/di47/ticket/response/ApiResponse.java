package lk.di47.ticket.response;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> failed(String message, T data) {
        return new ApiResponse<>(false, message, data, LocalDateTime.now());
    }
}
