package lk.di47.ticket.feature.ticket.dto;

import java.time.LocalDateTime;

public record TicketReplyResponse(
        Long id,
        Long senderUserId,
        String message,
        LocalDateTime createdAt
) {
}
