package lk.di47.ticket.repository;

import lk.di47.ticket.entity.TicketStatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketStatusMasterRepository extends JpaRepository<TicketStatusMaster, Long> {
    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
