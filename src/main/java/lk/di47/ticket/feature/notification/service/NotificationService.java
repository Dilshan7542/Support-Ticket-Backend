package lk.di47.ticket.feature.notification.service;

import lk.di47.ticket.util.enums.NotificationType;

public interface NotificationService {
    void notifyUserAsync(Long userId, String title, String message, NotificationType type);
}
