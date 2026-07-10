package lk.di47.ticket.feature.ticketcategory.service.impl;

import lk.di47.ticket.entity.Department;
import lk.di47.ticket.entity.TicketCategory;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.ticketcategory.dto.*;
import lk.di47.ticket.feature.ticketcategory.service.TicketCategoryService;
import lk.di47.ticket.repository.CompanyRepository;
import lk.di47.ticket.repository.DepartmentRepository;
import lk.di47.ticket.repository.TicketCategoryRepository;
import lk.di47.ticket.response.PageResponse;
import lk.di47.ticket.util.PaginationUtil;
import lk.di47.ticket.util.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
public class TicketCategoryServiceImpl implements TicketCategoryService {
    private final TicketCategoryRepository ticketCategoryRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public TicketCategoryResponse create(CreateTicketCategoryRequest request) {
        ParentMapping parentMapping = validateParents(request.companyId(), request.departmentId());
        if (ticketCategoryRepository.existsByCode(request.code())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Ticket category code already exists");
        }

        TicketCategory category = new TicketCategory();
        category.setCompanyId(parentMapping.companyId());
        category.setDepartmentId(parentMapping.departmentId());
        category.setName(request.name());
        category.setCode(request.code());
        category.setDescription(request.description());
        category.setStatus(Status.ACTIVE);
        category.setCreatedAt(LocalDateTime.now());
        return toResponse(ticketCategoryRepository.save(category));
    }

    @Override
    public PageResponse<TicketCategoryResponse> list(ListTicketCategoryRequest request) {
        validateParents(request.companyId(), request.departmentId());
        Page<TicketCategory> categories = findCategories(request);
        return PageResponse.from(categories, this::toResponse);
    }

    @Override
    public TicketCategoryResponse detail(TicketCategoryDetailRequest request) {
        return toResponse(findCategory(request.categoryId()));
    }

    @Override
    @Transactional
    public TicketCategoryResponse update(UpdateTicketCategoryRequest request) {
        TicketCategory category = findCategory(request.categoryId());
        ParentMapping parentMapping = validateParents(request.companyId(), request.departmentId());
        if (ticketCategoryRepository.existsByCodeAndIdNot(request.code(), category.getId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Ticket category code already exists");
        }

        category.setCompanyId(parentMapping.companyId());
        category.setDepartmentId(parentMapping.departmentId());
        category.setName(request.name());
        category.setCode(request.code());
        category.setDescription(request.description());
        if (request.status() != null) {
            category.setStatus(request.status());
        }
        category.setUpdatedAt(LocalDateTime.now());
        return toResponse(ticketCategoryRepository.save(category));
    }

    private TicketCategory findCategory(Long categoryId) {
        return ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Ticket category not found"));
    }

    private ParentMapping validateParents(Long companyId, Long departmentId) {
        Long resolvedCompanyId = companyId;
        if (departmentId != null) {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new NotFoundException("Department not found"));
            if (resolvedCompanyId == null) {
                resolvedCompanyId = department.getCompanyId();
            } else if (department.getCompanyId() != null && !resolvedCompanyId.equals(department.getCompanyId())) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "Department does not belong to selected company");
            }
        }
        if (resolvedCompanyId != null && !companyRepository.existsById(resolvedCompanyId)) {
            throw new NotFoundException("Company not found");
        }
        return new ParentMapping(resolvedCompanyId, departmentId);
    }

    private TicketCategoryResponse toResponse(TicketCategory category) {
        return new TicketCategoryResponse(
                category.getId(),
                category.getCompanyId(),
                category.getDepartmentId(),
                category.getName(),
                category.getCode(),
                category.getDescription(),
                category.getStatus()
        );
    }

    private Page<TicketCategory> findCategories(ListTicketCategoryRequest request) {
        var pageable = PaginationUtil.toPageable(request.page(), request.size());
        if (request.companyId() != null && request.departmentId() != null) {
            return ticketCategoryRepository.findByCompanyIdAndDepartmentId(request.companyId(), request.departmentId(), pageable);
        }
        if (request.departmentId() != null) {
            return ticketCategoryRepository.findByDepartmentId(request.departmentId(), pageable);
        }
        if (request.companyId() != null) {
            return ticketCategoryRepository.findByCompanyId(request.companyId(), pageable);
        }
        return ticketCategoryRepository.findAll(pageable);
    }

    private record ParentMapping(Long companyId, Long departmentId) {
    }
}
