package lk.di47.ticket.feature.vendor.service;

import lk.di47.ticket.feature.vendor.dto.VendorDetailRequest;
import lk.di47.ticket.feature.vendor.dto.VendorResponse;
import lk.di47.ticket.feature.vendor.dto.CreateVendorRequest;
import lk.di47.ticket.feature.vendor.dto.ListVendorRequest;
import lk.di47.ticket.feature.vendor.dto.UpdateVendorRequest;
import lk.di47.ticket.response.PageResponse;

public interface VendorService {
    VendorResponse create(CreateVendorRequest request);

    PageResponse<VendorResponse> list(ListVendorRequest request);

    VendorResponse detail(VendorDetailRequest request);

    VendorResponse update(UpdateVendorRequest request);
}
