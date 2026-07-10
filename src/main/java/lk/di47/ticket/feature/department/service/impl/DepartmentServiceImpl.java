package lk.di47.ticket.feature.department.service.impl;

import lk.di47.ticket.entity.Company;
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
import lk.di47.ticket.repository.CompanyRepository;
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
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {
        validateCompany(request.companyId());
        if (existsDepartmentName(request.companyId(), request.name())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Department already exists");
        }
        if (existsDepartmentCode(request.companyId(), request.code())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Department code already exists");
        }

        Department department = new Department();
        department.setName(request.name());
        department.setCode(request.code());
        department.setCompanyId(request.companyId());
        department.setDescription(request.description());
        department.setStatus(Status.ACTIVE);
        department.setCreatedAt(LocalDateTime.now());
        return toResponse(departmentRepository.save(department));
    }

    @Override
    public PageResponse<DepartmentResponse> list(ListDepartmentRequest request) {
        validateCompany(request.companyId());
        Page<Department> departments = request.companyId() == null
                ? departmentRepository.findAll(PaginationUtil.toPageable(request.page(), request.size()))
                : departmentRepository.findByCompanyId(request.companyId(), PaginationUtil.toPageable(request.page(), request.size()));
        Map<Long, Company> companiesById = loadCompaniesById(departments.getContent());
        return PageResponse.from(departments, department -> toResponse(department, companiesById.get(department.getCompanyId())));
    }

    @Override
    public DepartmentResponse detail(DepartmentDetailRequest request) {
        return toResponse(findDepartment(request.departmentId()));
    }

    @Override
    @Transactional
    public DepartmentResponse update(UpdateDepartmentRequest request) {
        Department department = findDepartment(request.departmentId());
        validateCompany(request.companyId());
        if (existsDepartmentName(request.companyId(), request.name(), department.getId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Department already exists");
        }
        if (existsDepartmentCode(request.companyId(), request.code(), department.getId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Department code already exists");
        }
        department.setName(request.name());
        department.setCode(request.code());
        department.setCompanyId(request.companyId());
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
        Company company = department.getCompanyId() == null
                ? null
                : companyRepository.findById(department.getCompanyId()).orElse(null);
        return toResponse(department, company);
    }

    private DepartmentResponse toResponse(Department department, Company company) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getCompanyId(),
                company == null ? null : company.getName(),
                department.getDescription(),
                department.getStatus()
        );
    }

    private void validateCompany(Long companyId) {
        if (companyId != null && !companyRepository.existsById(companyId)) {
            throw new NotFoundException("Company not found");
        }
    }

    private boolean existsDepartmentName(Long companyId, String name) {
        if (companyId == null) {
            return departmentRepository.existsByName(name);
        }
        return departmentRepository.existsByCompanyIdAndName(companyId, name);
    }

    private boolean existsDepartmentName(Long companyId, String name, Long excludedDepartmentId) {
        if (companyId == null) {
            return departmentRepository.existsByNameAndIdNot(name, excludedDepartmentId);
        }
        return departmentRepository.existsByCompanyIdAndNameAndIdNot(companyId, name, excludedDepartmentId);
    }

    private boolean existsDepartmentCode(Long companyId, String code) {
        if (companyId == null) {
            return departmentRepository.existsByCode(code);
        }
        return departmentRepository.existsByCompanyIdAndCode(companyId, code);
    }

    private boolean existsDepartmentCode(Long companyId, String code, Long excludedDepartmentId) {
        if (companyId == null) {
            return departmentRepository.existsByCodeAndIdNot(code, excludedDepartmentId);
        }
        return departmentRepository.existsByCompanyIdAndCodeAndIdNot(companyId, code, excludedDepartmentId);
    }

    private Map<Long, Company> loadCompaniesById(List<Department> departments) {
        List<Long> companyIds = departments.stream()
                .map(Department::getCompanyId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (companyIds.isEmpty()) {
            return Map.of();
        }
        return companyRepository.findByIdIn(companyIds).stream()
                .collect(Collectors.toMap(Company::getId, Function.identity()));
    }
}
