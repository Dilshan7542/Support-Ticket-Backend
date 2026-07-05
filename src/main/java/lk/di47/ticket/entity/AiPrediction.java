package lk.di47.ticket.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ai_prediction")
public class AiPrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ticketId;

    private String predictedCategory;
    private String predictedPriority;
    private Long suggestedDepartmentId;
    private BigDecimal confidenceScore;

    @Lob
    private String rawResponse;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
