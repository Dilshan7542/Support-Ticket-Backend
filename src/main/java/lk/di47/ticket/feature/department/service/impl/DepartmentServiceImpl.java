package lk.di47.ticket.feature.department.service.impl;

import lk.di47.ticket.entity.Vendor;
import lk.di47.ticket.entity.Department;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.department.dto.CreateDepartmentRequest;
import lk.di47.ticket.feature.department.dto.DepartmentDetailRequest;
import lk.di47.ticket.feature.department.dto.DepartmentResponse;
import lk.di47.ticket.feature.department.dto.ListDepartmentRequest;
import lk.di47.ticket.feature.department.dto.UpdateDepartmentRequest;
import lk.di47.ticket.feature.department.service.DepartmentService;
import lk.di47.ticket.repository.VendorRepository;
import lk.di47.ticket.repository.DepartmentRepository;
import lk.di47.ticket.response.PageResponse;
import lk.di47.ticket.util.PaginationUtil;
import lk.di47.ticket.util.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final VendorRepository vendorRepository;

    @Override
    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {
        validateVendor(request.vendorId());
        if (existsDepartmentName(request.vendorId(), request.name())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Department already exists");
        }
        if (existsDepartmentCode(request.vendorId(), request.code())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Department code already exists");
        }

        Department department = new Department();
        department.setName(request.name());
        department.setCode(request.code());
        department.setVendorId(request.vendorId());
        department.setDescription(request.description());
        department.setStatus(Status.ACTIVE);
        department.setCreatedAt(LocalDateTime.now());
        return toResponse(departmentRepository.save(department));
    }

    @Override
    public PageResponse<DepartmentResponse> list(ListDepartmentRequest request) {
        validateVendor(request.vendorId());
        Page<Department> departments = request.vendorId() == null
                ? departmentRepository.findAll(PaginationUtil.toPageable(request.page(), request.size()))
                : departmentRepository.findByVendorId(request.vendorId(), PaginationUtil.toPageable(request.page(), request.size()));
        Map<Long, Vendor> vendorsById = loadVendorsById(departments.getContent());
        return PageResponse.from(departments, department -> toResponse(department, vendorsById.get(department.getVendorId())));
    }

    @Override
    public DepartmentResponse detail(DepartmentDetailRequest request) {
        return toResponse(findDepartment(request.departmentId()));
    }

    @Override
    @Transactional
    public DepartmentResponse update(UpdateDepartmentRequest request) {
        Department department = findDepartment(request.departmentId());
        validateVendor(request.vendorId());
        if (existsDepartmentName(request.vendorId(), request.name(), department.getId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Department already exists");
        }
        if (existsDepartmentCode(request.vendorId(), request.code(), department.getId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Department code already exists");
        }
        department.setName(request.name());
        department.setCode(request.code());
        department.setVendorId(request.vendorId());
        department.setDescription(request.description());
        if (request.status() != null) {
            department.setStatus(request.status());
        }
        department.setUpdatedAt(LocalDateTime.now());
        return toResponse(departmentRepository.save(department));
    }

    private Department findDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found"));
    }

    private DepartmentResponse toResponse(Department department) {
        Vendor vendor = department.getVendorId() == null
                ? null
                : vendorRepository.findById(department.getVendorId()).orElse(null);
        return toResponse(department, vendor);
    }

    private DepartmentResponse toResponse(Department department, Vendor vendor) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getVendorId(),
                vendor == null ? null : vendor.getName(),
                department.getDescription(),
                department.getStatus()
        );
    }

    private void validateVendor(Long vendorId) {
        if (vendorId == null) {
            return;
        }
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new NotFoundException("Vendor not found"));
        if (vendor.getStatus() != Status.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Vendor is not active");
        }
    }

    private boolean existsDepartmentName(Long vendorId, String name) {
        if (vendorId == null) {
            return departmentRepository.existsByName(name);
        }
        return departmentRepository.existsByVendorIdAndName(vendorId, name);
    }

    private boolean existsDepartmentName(Long vendorId, String name, Long excludedDepartmentId) {
        if (vendorId == null) {
            return departmentRepository.existsByNameAndIdNot(name, excludedDepartmentId);
        }
        return departmentRepository.existsByVendorIdAndNameAndIdNot(vendorId, name, excludedDepartmentId);
    }

    private boolean existsDepartmentCode(Long vendorId, String code) {
        if (vendorId == null) {
            return departmentRepository.existsByCode(code);
        }
        return departmentRepository.existsByVendorIdAndCode(vendorId, code);
    }

    private boolean existsDepartmentCode(Long vendorId, String code, Long excludedDepartmentId) {
        if (vendorId == null) {
            return departmentRepository.existsByCodeAndIdNot(code, excludedDepartmentId);
        }
        return departmentRepository.existsByVendorIdAndCodeAndIdNot(vendorId, code, excludedDepartmentId);
    }

    private Map<Long, Vendor> loadVendorsById(List<Department> departments) {
        List<Long> vendorIds = departments.stream()
                .map(Department::getVendorId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (vendorIds.isEmpty()) {
            return Map.of();
        }
        return vendorRepository.findByIdIn(vendorIds).stream()
                .collect(Collectors.toMap(Vendor::getId, Function.identity()));
    }
}
