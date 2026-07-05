package lk.di47.ticket.feature.ticketcategory.service.impl;

import lk.di47.ticket.entity.TicketCategory;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.ticketcategory.dto.*;
import lk.di47.ticket.feature.ticketcategory.service.TicketCategoryService;
import lk.di47.ticket.repository.CompanyRepository;
import lk.di47.ticket.repository.DepartmentRepository;
import lk.di47.ticket.repository.TicketCategoryRepository;
import lk.di47.ticket.util.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketCategoryServiceImpl implements TicketCategoryService {
    private final TicketCategoryRepository ticketCategoryRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public TicketCategoryResponse create(CreateTicketCategoryRequest request) {
        validateParents(request.companyId(), request.departmentId());
        if (ticketCategoryRepository.existsByCode(request.code())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Ticket category code already exists");
        }

        TicketCategory category = new TicketCategory();
        category.setCompanyId(request.companyId());
        category.setDepartmentId(request.departmentId());
        category.setName(request.name());
        category.setCode(request.code());
        category.setDescription(request.description());
        category.setStatus(Status.ACTIVE);
        category.setCreatedAt(LocalDateTime.now());
        return toResponse(ticketCategoryRepository.save(category));
    }

    @Override
    public List<TicketCategoryResponse> list() {
        return ticketCategoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public TicketCategoryResponse detail(TicketCategoryDetailRequest request) {
        return toResponse(findCategory(request.categoryId()));
    }

    @Override
    @Transactional
    public TicketCategoryResponse update(UpdateTicketCategoryRequest request) {
        TicketCategory category = findCategory(request.categoryId());
        validateParents(request.companyId(), request.departmentId());
        if (ticketCategoryRepository.existsByCodeAndIdNot(request.code(), category.getId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Ticket category code already exists");
        }

        category.setCompanyId(request.companyId());
        category.setDepartmentId(request.departmentId());
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

    private void validateParents(Long companyId, Long departmentId) {
        if (companyId != null && !companyRepository.existsById(companyId)) {
            throw new NotFoundException("Company not found");
        }
        if (departmentId != null && !departmentRepository.existsById(departmentId)) {
            throw new NotFoundException("Department not found");
        }
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
}
