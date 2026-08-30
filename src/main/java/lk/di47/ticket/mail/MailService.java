package lk.di47.ticket.mail;

import lk.di47.ticket.entity.MailLog;
import lk.di47.ticket.repository.MailLogRepository;
import lk.di47.ticket.util.enums.MailStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Log4j2
public class MailService {
    private final JavaMailSender javaMailSender;
    private final MailLogRepository mailLogRepository;

    @Value("${app.mail.from}")
    private String from;

    @Async
    public void send(MailRequest request) {
        MailLog mailLog = new MailLog();
        mailLog.setRecipient(request.to());
        mailLog.setSubject(request.subject());
        mailLog.setCreatedAt(LocalDateTime.now());
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(request.to());
            message.setSubject(request.subject());
            message.setText(request.body());
            javaMailSender.send(message);
            mailLog.setStatus(MailStatus.SENT);
        } catch (Exception exception) {
            log.error("Mail send failed for recipient {} -> {}", request.to(), exception.getMessage(), exception);
            mailLog.setStatus(MailStatus.FAILED);
            mailLog.setErrorMessage(exception.getMessage());
        }
        mailLogRepository.save(mailLog);
    }
}
