package lk.di47.ticket.repository;

import lk.di47.ticket.entity.TicketCategory;
import lk.di47.ticket.util.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {
    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Optional<TicketCategory> findByCodeAndStatus(String code, Status status);

    List<TicketCategory> findByCodeIn(Collection<String> codes);

    List<TicketCategory> findByIdIn(Collection<Long> ids);

    List<TicketCategory> findByNameContainingIgnoreCase(String name);
}
