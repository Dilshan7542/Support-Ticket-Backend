package lk.di47.ticket.feature.notification.dto;

import lk.di47.ticket.util.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        String title,
        String message,
        NotificationType notificationType,
        boolean readStatus,
        LocalDateTime createdAt
) {
}
