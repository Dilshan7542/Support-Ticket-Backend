package lk.di47.ticket.repository;

import lk.di47.ticket.entity.TicketCategoryDepartmentMapping;
import lk.di47.ticket.util.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketCategoryDepartmentMappingRepository extends JpaRepository<TicketCategoryDepartmentMapping, Long> {
    boolean existsByCategoryIdAndStatus(Long categoryId, Status status);

    boolean existsByCategoryIdAndStatusAndIdNot(Long categoryId, Status status, Long id);

    Page<TicketCategoryDepartmentMapping> findByCategoryId(Long categoryId, Pageable pageable);

    Optional<TicketCategoryDepartmentMapping> findByCategoryIdAndStatus(Long categoryId, Status status);
}
