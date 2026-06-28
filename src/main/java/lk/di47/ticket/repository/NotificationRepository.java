package lk.di47.ticket.repository;

import lk.di47.ticket.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);

    long countByReadStatus(boolean readStatus);
}
