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
@Table(name = "ticket_reply")
public class TicketReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ticketId;

    @Column(nullable = false)
    private Long senderUserId;

    @Lob
    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
