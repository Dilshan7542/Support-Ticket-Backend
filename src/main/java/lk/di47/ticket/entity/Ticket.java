package lk.di47.ticket.entity;

import jakarta.persistence.*;
import lk.di47.ticket.util.enums.TicketStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String ticketNo;

    @Column(nullable = false)
    private Long customerId;

    private Long companyId;
    private Long departmentId;
    private Long categoryId;
    private Long assignedStaffId;

    @Column(nullable = false, length = 150)
    private String subject;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(name = "category_code", length = 64)
    private String categoryCode;

    @Column(nullable = false, length = 20)
    private String priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
