package lk.di47.ticket.repository;

import lk.di47.ticket.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<Company> findByIdIn(Collection<Long> ids);

    List<Company> findByNameContainingIgnoreCase(String name);
}
