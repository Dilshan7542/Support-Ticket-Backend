package lk.di47.ticket.repository;

import lk.di47.ticket.entity.AiPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiPredictionRepository extends JpaRepository<AiPrediction, Long> {
    Optional<AiPrediction> findTopByTicketIdOrderByCreatedAtDesc(Long ticketId);
}
