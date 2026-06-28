package lk.di47.ticket.repository;

import lk.di47.ticket.entity.Department;
import lk.di47.ticket.util.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByName(String name);

    long countByStatus(Status status);
}
