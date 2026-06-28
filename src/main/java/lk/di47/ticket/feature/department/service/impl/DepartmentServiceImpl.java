package lk.di47.ticket.feature.department.service.impl;

import lk.di47.ticket.entity.Department;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.department.dto.CreateDepartmentRequest;
import lk.di47.ticket.feature.department.dto.DepartmentDetailRequest;
import lk.di47.ticket.feature.department.dto.DepartmentResponse;
import lk.di47.ticket.feature.department.dto.UpdateDepartmentRequest;
import lk.di47.ticket.feature.department.service.DepartmentService;
import lk.di47.ticket.repository.DepartmentRepository;
import lk.di47.ticket.util.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {
        if (departmentRepository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Department already exists");
        }

        Department department = new Department();
        department.setName(request.name());
        department.setDescription(request.description());
        department.setStatus(Status.ACTIVE);
        department.setCreatedAt(LocalDateTime.now());
        return toResponse(departmentRepository.save(department));
    }

    @Override
    public List<DepartmentResponse> list() {
        return departmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public DepartmentResponse detail(DepartmentDetailRequest request) {
        return toResponse(findDepartment(request.departmentId()));
    }

    @Override
    @Transactional
    public DepartmentResponse update(UpdateDepartmentRequest request) {
        Department department = findDepartment(request.departmentId());
        department.setName(request.name());
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
        return new DepartmentResponse(department.getId(), department.getName(), department.getDescription(), department.getStatus());
    }
}
