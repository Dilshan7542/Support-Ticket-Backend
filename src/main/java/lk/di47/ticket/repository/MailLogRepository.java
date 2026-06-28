package lk.di47.ticket.repository;

import lk.di47.ticket.entity.MailLog;
import lk.di47.ticket.util.enums.MailStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailLogRepository extends JpaRepository<MailLog, Long> {
    long countByStatus(MailStatus status);
}
