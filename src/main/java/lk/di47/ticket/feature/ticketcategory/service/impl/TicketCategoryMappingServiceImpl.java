package lk.di47.ticket.feature.ticketcategory.service.impl;

import lk.di47.ticket.entity.Department;
import lk.di47.ticket.entity.TicketCategory;
import lk.di47.ticket.entity.TicketCategoryDepartmentMapping;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.ticketcategory.dto.CreateTicketCategoryMappingRequest;
import lk.di47.ticket.feature.ticketcategory.dto.ListTicketCategoryMappingRequest;
import lk.di47.ticket.feature.ticketcategory.dto.TicketCategoryMappingDetailRequest;
import lk.di47.ticket.feature.ticketcategory.dto.TicketCategoryMappingResponse;
import lk.di47.ticket.feature.ticketcategory.dto.UpdateTicketCategoryMappingRequest;
import lk.di47.ticket.feature.ticketcategory.service.TicketCategoryMappingService;
import lk.di47.ticket.repository.DepartmentRepository;
import lk.di47.ticket.repository.TicketCategoryDepartmentMappingRepository;
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
public class TicketCategoryMappingServiceImpl implements TicketCategoryMappingService {
    private final TicketCategoryDepartmentMappingRepository mappingRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public TicketCategoryMappingResponse create(CreateTicketCategoryMappingRequest request) {
        validateMappingParents(request.categoryId(), request.departmentId());
        if (mappingRepository.existsByCategoryIdAndStatus(request.categoryId(), Status.ACTIVE)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Active category mapping already exists");
        }

        TicketCategoryDepartmentMapping mapping = new TicketCategoryDepartmentMapping();
        mapping.setCategoryId(request.categoryId());
        mapping.setDepartmentId(request.departmentId());
        mapping.setStatus(Status.ACTIVE);
        mapping.setCreatedAt(LocalDateTime.now());
        return toResponse(mappingRepository.save(mapping));
    }

    @Override
    public PageResponse<TicketCategoryMappingResponse> list(ListTicketCategoryMappingRequest request) {
        Page<TicketCategoryDepartmentMapping> mappings = findMappings(request);
        return PageResponse.from(mappings, this::toResponse);
    }

    @Override
    public TicketCategoryMappingResponse detail(TicketCategoryMappingDetailRequest request) {
        return toResponse(findMapping(request.mappingId()));
    }

    @Override
    @Transactional
    public TicketCategoryMappingResponse update(UpdateTicketCategoryMappingRequest request) {
        TicketCategoryDepartmentMapping mapping = findMapping(request.mappingId());
        validateMappingParents(request.categoryId(), request.departmentId());
        Status requestedStatus = request.status() == null ? mapping.getStatus() : request.status();
        if (Status.ACTIVE.equals(requestedStatus) && mappingRepository.existsByCategoryIdAndStatusAndIdNot(
                request.categoryId(),
                Status.ACTIVE,
                request.mappingId()
        )) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Active category mapping already exists");
        }

        mapping.setCategoryId(request.categoryId());
        mapping.setDepartmentId(request.departmentId());
        if (request.status() != null) {
            mapping.setStatus(request.status());
        }
        mapping.setUpdatedAt(LocalDateTime.now());
        return toResponse(mappingRepository.save(mapping));
    }

    private Page<TicketCategoryDepartmentMapping> findMappings(ListTicketCategoryMappingRequest request) {
        var pageable = PaginationUtil.toPageable(request.page(), request.size());
        if (request.categoryId() != null) {
            return mappingRepository.findByCategoryId(request.categoryId(), pageable);
        }
        return mappingRepository.findAll(pageable);
    }

    private TicketCategoryDepartmentMapping findMapping(Long mappingId) {
        return mappingRepository.findById(mappingId)
                .orElseThrow(() -> new NotFoundException("Ticket category mapping not found"));
    }

    private void validateMappingParents(Long categoryId, Long departmentId) {
        TicketCategory category = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Ticket category not found"));
        if (category.getStatus() != Status.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Ticket category is not active");
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found"));
        if (department.getStatus() != Status.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Department is not active");
        }
    }

    private TicketCategoryMappingResponse toResponse(TicketCategoryDepartmentMapping mapping) {
        TicketCategory category = ticketCategoryRepository.findById(mapping.getCategoryId()).orElse(null);
        Department department = departmentRepository.findById(mapping.getDepartmentId()).orElse(null);
        return new TicketCategoryMappingResponse(
                mapping.getId(),
                mapping.getCategoryId(),
                category == null ? null : category.getCode(),
                category == null ? null : category.getName(),
                mapping.getDepartmentId(),
                department == null ? null : department.getName(),
                mapping.getStatus()
        );
    }
}
