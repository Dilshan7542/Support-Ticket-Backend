package lk.di47.ticket.crypto;

import jakarta.validation.constraints.NotBlank;

public record EncryptedPayload(
        @NotBlank(message = "iv is required")
        String iv,

        @NotBlank(message = "cipherText is required")
        String cipherText
) {
}
