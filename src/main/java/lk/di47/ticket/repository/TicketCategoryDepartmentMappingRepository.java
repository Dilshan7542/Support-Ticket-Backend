package lk.di47.ticket.repository;

import lk.di47.ticket.entity.TicketCategoryDepartmentMapping;
import lk.di47.ticket.util.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketCategoryDepartmentMappingRepository extends JpaRepository<TicketCategoryDepartmentMapping, Long> {
    boolean existsByCompanyIdAndCategoryId(Long companyId, Long categoryId);

    boolean existsByCompanyIdAndCategoryIdAndIdNot(Long companyId, Long categoryId, Long id);

    Optional<TicketCategoryDepartmentMapping> findByCompanyIdAndCategoryIdAndStatus(
            Long companyId,
            Long categoryId,
            Status status
    );

    Page<TicketCategoryDepartmentMapping> findByCompanyId(Long companyId, Pageable pageable);

    Page<TicketCategoryDepartmentMapping> findByCategoryId(Long categoryId, Pageable pageable);

    Page<TicketCategoryDepartmentMapping> findByCompanyIdAndCategoryId(Long companyId, Long categoryId, Pageable pageable);
}
