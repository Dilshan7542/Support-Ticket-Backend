package lk.di47.ticket.feature.notification.service.impl;

import lk.di47.ticket.entity.Notification;
import lk.di47.ticket.entity.User;
import lk.di47.ticket.feature.notification.service.NotificationService;
import lk.di47.ticket.mail.MailRequest;
import lk.di47.ticket.mail.MailService;
import lk.di47.ticket.repository.NotificationRepository;
import lk.di47.ticket.repository.UserRepository;
import lk.di47.ticket.util.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Override
    @Async
    @Transactional
    public void notifyUserAsync(Long userId, String title, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(type);
        notification.setReadStatus(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        userRepository.findById(userId)
                .map(User::getEmail)
                .filter(email -> email != null && !email.isBlank())
                .ifPresent(email -> mailService.send(new MailRequest(email, title, message)));
    }
}
