package lk.di47.ticket.entity;

import jakarta.persistence.*;
import lk.di47.ticket.util.enums.MailStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "mail_logs")
public class MailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MailStatus status;

    @Lob
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
