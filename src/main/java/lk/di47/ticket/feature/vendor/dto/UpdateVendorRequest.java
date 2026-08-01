package lk.di47.ticket.feature.vendor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.di47.ticket.util.enums.Status;

public record UpdateVendorRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "vendorId is required")
        Long vendorId,

        @NotBlank(message = "Vendor name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "Vendor code is required")
        @Size(max = 50)
        String code,

        @Size(max = 500)
        String description,

        Status status
) {
}
