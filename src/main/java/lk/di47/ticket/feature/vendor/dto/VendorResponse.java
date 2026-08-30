package lk.di47.ticket.feature.vendor.dto;

import lk.di47.ticket.util.enums.Status;

public record VendorResponse(
        Long id,
        String name,
        String code,
        String description,
        Status status
) {
}
