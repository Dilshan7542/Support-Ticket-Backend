package lk.di47.ticket.feature.security.dto;

import jakarta.validation.constraints.NotBlank;

public record KeyExchangeRequest(
        @NotBlank(message = "clientPublicKey is required")
        String clientPublicKey
) {
}
