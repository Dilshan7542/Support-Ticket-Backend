package lk.di47.ticket.repository;

import lk.di47.ticket.entity.Ticket;
import lk.di47.ticket.util.enums.TicketPriority;
import lk.di47.ticket.util.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByTicketNo(String ticketNo);

    long countByStatus(TicketStatus status);

    long countByPriority(TicketPriority priority);

    long countByDepartmentIdIsNull();

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    List<Ticket> findTop10ByOrderByCreatedAtDesc();

    List<Ticket> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    List<Ticket> findByCustomerId(Long customerId);
}
