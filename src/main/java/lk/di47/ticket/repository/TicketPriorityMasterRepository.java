package lk.di47.ticket.repository;

import lk.di47.ticket.entity.TicketPriorityMaster;
import lk.di47.ticket.util.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketPriorityMasterRepository extends JpaRepository<TicketPriorityMaster, Long> {
    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Optional<TicketPriorityMaster> findByCodeAndStatus(String code, Status status);

    List<TicketPriorityMaster> findByStatusOrderByIdAsc(Status status);
}
