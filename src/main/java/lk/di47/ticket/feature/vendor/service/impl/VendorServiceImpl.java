package lk.di47.ticket.feature.vendor.service.impl;

import lk.di47.ticket.entity.Vendor;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.vendor.dto.VendorDetailRequest;
import lk.di47.ticket.feature.vendor.dto.VendorResponse;
import lk.di47.ticket.feature.vendor.dto.CreateVendorRequest;
import lk.di47.ticket.feature.vendor.dto.ListVendorRequest;
import lk.di47.ticket.feature.vendor.dto.UpdateVendorRequest;
import lk.di47.ticket.feature.vendor.service.VendorService;
import lk.di47.ticket.repository.VendorRepository;
import lk.di47.ticket.response.PageResponse;
import lk.di47.ticket.util.PaginationUtil;
import lk.di47.ticket.util.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {
    private final VendorRepository vendorRepository;

    @Override
    @Transactional
    public VendorResponse create(CreateVendorRequest request) {
        if (vendorRepository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Vendor already exists");
        }
        if (vendorRepository.existsByCode(request.code())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Vendor code already exists");
        }

        Vendor vendor = new Vendor();
        vendor.setName(request.name());
        vendor.setCode(request.code());
        vendor.setDescription(request.description());
        vendor.setStatus(Status.ACTIVE);
        vendor.setCreatedAt(LocalDateTime.now());
        return toResponse(vendorRepository.save(vendor));
    }

    @Override
    public PageResponse<VendorResponse> list(ListVendorRequest request) {
        return PageResponse.from(
                vendorRepository.findAll(PaginationUtil.toPageable(request.page(), request.size())),
                this::toResponse
        );
    }

    @Override
    public VendorResponse detail(VendorDetailRequest request) {
        return toResponse(findVendor(request.vendorId()));
    }

    @Override
    @Transactional
    public VendorResponse update(UpdateVendorRequest request) {
        Vendor vendor = findVendor(request.vendorId());
        if (vendorRepository.existsByNameAndIdNot(request.name(), vendor.getId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Vendor already exists");
        }
        if (vendorRepository.existsByCodeAndIdNot(request.code(), vendor.getId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Vendor code already exists");
        }
        vendor.setName(request.name());
        vendor.setCode(request.code());
        vendor.setDescription(request.description());
        if (request.status() != null) {
            vendor.setStatus(request.status());
        }
        vendor.setUpdatedAt(LocalDateTime.now());
        return toResponse(vendorRepository.save(vendor));
    }

    private Vendor findVendor(Long vendorId) {
        return vendorRepository.findById(vendorId)
                .orElseThrow(() -> new NotFoundException("Vendor not found"));
    }

    private VendorResponse toResponse(Vendor vendor) {
        return new VendorResponse(vendor.getId(), vendor.getName(), vendor.getCode(), vendor.getDescription(), vendor.getStatus());
    }
}
