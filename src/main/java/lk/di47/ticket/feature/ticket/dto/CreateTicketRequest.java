package lk.di47.ticket.feature.ticket.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotNull(message = "userId is required")
        Long userId,

        Long companyId,

        @Size(max = 150)
        String subject,

        @Size(max = 2000)
        String description,

        @Size(max = 64)
        String categoryCode,

        @Size(max = 150)
        String title,

        @Size(max = 2000)
        String message
) {
    public String effectiveSubject() {
        return hasText(subject) ? subject : title;
    }

    public String effectiveDescription() {
        return hasText(description) ? description : message;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
