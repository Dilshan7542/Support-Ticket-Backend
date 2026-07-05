package lk.di47.ticket.feature.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotBlank(message = "Company name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "Company code is required")
        @Size(max = 30)
        String code,

        @Size(max = 255)
        String description
) {
}
