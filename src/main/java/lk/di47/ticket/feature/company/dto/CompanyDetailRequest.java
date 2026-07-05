package lk.di47.ticket.feature.company.dto;

import jakarta.validation.constraints.NotNull;

public record CompanyDetailRequest(
        @NotNull(message = "companyId is required")
        Long companyId
) {
}
