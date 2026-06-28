package lk.di47.ticket.feature.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 100)
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100)
        String password,

        @NotBlank(message = "Full name is required")
        @Size(max = 150)
        String fullName,

        @Email
        @Size(max = 150)
        String email,

        @Size(max = 30)
        String phone
) {
}
