package lk.di47.ticket.config;

import lk.di47.ticket.entity.User;
import lk.di47.ticket.repository.UserRepository;
import lk.di47.ticket.util.enums.Status;
import lk.di47.ticket.util.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class StartupDataConfig {

    @Bean
    CommandLineRunner createDefaultAdmin(UserRepository userRepository,
                                         PasswordEncoder passwordEncoder,
                                         @Value("${app.default-admin.username:admin}") String username,
                                         @Value("${app.default-admin.password:admin123}") String password) {
        return args -> {
            if (!userRepository.existsByUsername(username)) {
                User user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(password));
                user.setFullName("System Admin");
                user.setEmail("admin@aiticket.local");
                user.setRole(UserRole.SUPER_ADMIN);
                user.setStatus(Status.ACTIVE);
                user.setCreatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
        };
    }
}
