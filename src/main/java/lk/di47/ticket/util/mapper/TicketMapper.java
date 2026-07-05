package lk.di47.ticket.util.mapper;

import lk.di47.ticket.entity.Ticket;
import lk.di47.ticket.feature.ticket.dto.TicketResponse;

import java.util.List;

public final class TicketMapper {
    private TicketMapper() {
    }

    public static TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketNo(),
                ticket.getCustomerId(),
                ticket.getDepartmentId(),
                ticket.getAssignedStaffId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}
