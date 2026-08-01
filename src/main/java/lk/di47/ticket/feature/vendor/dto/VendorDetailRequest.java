package lk.di47.ticket.feature.vendor.dto;

import jakarta.validation.constraints.NotNull;

public record VendorDetailRequest(
        @NotNull(message = "vendorId is required")
        Long vendorId
) {
}
