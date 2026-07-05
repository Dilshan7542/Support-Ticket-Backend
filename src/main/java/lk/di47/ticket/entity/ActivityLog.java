package lk.di47.ticket.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "activity_log")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String traceId;
    private Long userId;
    private String httpMethod;
    private String endpoint;
    private String action;

    @Lob
    private String requestBody;

    @Lob
    private String responseBody;

    private Integer responseStatus;
    private String ipAddress;
    private String userAgent;
    private boolean encryptionEnabled;
    private Long executionTimeMs;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
