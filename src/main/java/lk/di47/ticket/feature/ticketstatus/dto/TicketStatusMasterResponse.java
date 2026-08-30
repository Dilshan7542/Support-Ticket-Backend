package lk.di47.ticket.feature.ticketstatus.dto;

import lk.di47.ticket.util.enums.Status;

public record TicketStatusMasterResponse(
        Long id,
        String name,
        String code,
        String description,
        Status status
) {
}
