package lk.di47.ticket.feature.ticketpriority.dto;

import lk.di47.ticket.util.enums.Status;

public record TicketPriorityResponse(
        Long id,
        String name,
        String code,
        String description,
        Status status
) {
}
