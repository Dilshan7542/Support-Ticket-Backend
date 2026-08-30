package lk.di47.ticket.feature.ticket.dto;

import lk.di47.ticket.util.enums.TicketStatus;

import java.time.LocalDateTime;

public record TicketTrackingResponse(
        Long id,
        TicketStatus previousStatus,
        TicketStatus newStatus,
        Long changedByUserId,
        String remark,
        LocalDateTime createdAt
) {
}
