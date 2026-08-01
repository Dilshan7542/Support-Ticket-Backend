package lk.di47.ticket.repository;

import lk.di47.ticket.entity.Department;
import lk.di47.ticket.util.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByVendorIdAndName(Long vendorId, String name);

    boolean existsByVendorIdAndNameAndIdNot(Long vendorId, String name, Long id);

    boolean existsByVendorIdAndCode(Long vendorId, String code);

    boolean existsByVendorIdAndCodeAndIdNot(Long vendorId, String code, Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    long countByStatus(Status status);

    Page<Department> findByVendorId(Long vendorId, Pageable pageable);

    List<Department> findByIdIn(Collection<Long> ids);

    List<Department> findByNameContainingIgnoreCase(String name);
}
