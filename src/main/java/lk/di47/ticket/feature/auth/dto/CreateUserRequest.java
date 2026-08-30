package lk.di47.ticket.feature.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.di47.ticket.util.enums.UserRole;

public record CreateUserRequest(
        @NotNull(message = "userId is required")
        Long userId,

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
        String phone,

        Long vendorId,

        @NotNull(message = "role is required")
        UserRole role
) {
}
