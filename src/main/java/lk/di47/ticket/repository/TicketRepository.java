package lk.di47.ticket.repository;

import lk.di47.ticket.entity.Ticket;
import lk.di47.ticket.util.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    Optional<Ticket> findByTicketNo(String ticketNo);

    long countByStatus(TicketStatus status);

    long countByPriority(String priority);

    long countByDepartmentIdIsNull();

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    List<Ticket> findTop10ByOrderByCreatedAtDesc();

    List<Ticket> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    Page<Ticket> findByCustomerId(Long customerId, Pageable pageable);
}
