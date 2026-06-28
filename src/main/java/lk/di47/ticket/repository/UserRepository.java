package lk.di47.ticket.repository;

import lk.di47.ticket.entity.User;
import lk.di47.ticket.util.enums.Status;
import lk.di47.ticket.util.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByRole(UserRole role);

    long countByStatus(Status status);
}
