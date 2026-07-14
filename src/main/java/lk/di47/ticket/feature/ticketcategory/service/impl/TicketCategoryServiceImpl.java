package lk.di47.ticket.feature.ticketcategory.service.impl;

import lk.di47.ticket.entity.TicketCategory;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.ticketcategory.dto.*;
import lk.di47.ticket.feature.ticketcategory.service.TicketCategoryService;
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

    @Override
    @Transactional
    public TicketCategoryResponse create(CreateTicketCategoryRequest request) {
        if (ticketCategoryRepository.existsByCode(request.code())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Ticket category code already exists");
        }

        TicketCategory category = new TicketCategory();
        category.setName(request.name());
        category.setCode(request.code());
        category.setDescription(request.description());
        category.setStatus(Status.ACTIVE);
        category.setCreatedAt(LocalDateTime.now());
        return toResponse(ticketCategoryRepository.save(category));
    }

    @Override
    public PageResponse<TicketCategoryResponse> list(ListTicketCategoryRequest request) {
        Page<TicketCategory> categories = ticketCategoryRepository.findAll(PaginationUtil.toPageable(request.page(), request.size()));
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
        if (ticketCategoryRepository.existsByCodeAndIdNot(request.code(), category.getId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Ticket category code already exists");
        }

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

    private TicketCategoryResponse toResponse(TicketCategory category) {
        return new TicketCategoryResponse(
                category.getId(),
                category.getName(),
                category.getCode(),
                category.getDescription(),
                category.getStatus()
        );
    }
}
