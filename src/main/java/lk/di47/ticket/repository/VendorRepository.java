package lk.di47.ticket.repository;

import lk.di47.ticket.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<Vendor> findByIdIn(Collection<Long> ids);

    List<Vendor> findByNameContainingIgnoreCase(String name);
}
