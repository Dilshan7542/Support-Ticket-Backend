package lk.di47.ticket.feature.company.dto;

import lk.di47.ticket.util.enums.Status;

public record CompanyResponse(
        Long id,
        String name,
        String code,
        String description,
        Status status
) {
}
