package lk.di47.ticket.feature.ticketpriority.service.impl;

import lk.di47.ticket.entity.TicketPriorityMaster;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.ticketpriority.dto.*;
import lk.di47.ticket.feature.ticketpriority.service.TicketPriorityService;
import lk.di47.ticket.repository.TicketPriorityMasterRepository;
import lk.di47.ticket.response.PageResponse;
import lk.di47.ticket.util.PaginationUtil;
import lk.di47.ticket.util.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TicketPriorityServiceImpl implements TicketPriorityService {
    private final TicketPriorityMasterRepository ticketPriorityMasterRepository;

    @Override
    @Transactional
    public TicketPriorityResponse create(CreateTicketPriorityRequest request) {
        String code = normalizeCode(request.code());
        if (ticketPriorityMasterRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Ticket priority code already exists");
        }

        TicketPriorityMaster priority = new TicketPriorityMaster();
        priority.setName(request.name().trim());
        priority.setCode(code);
        priority.setDescription(request.description());
        priority.setStatus(Status.ACTIVE);
        priority.setCreatedAt(LocalDateTime.now());
        return toResponse(ticketPriorityMasterRepository.save(priority));
    }

    @Override
    public PageResponse<TicketPriorityResponse> list(ListTicketPriorityRequest request) {
        return PageResponse.from(
                ticketPriorityMasterRepository.findAll(PaginationUtil.toPageable(request.page(), request.size())),
                this::toResponse
        );
    }

    @Override
    public TicketPriorityResponse detail(TicketPriorityDetailRequest request) {
        return toResponse(findPriority(request.priorityId()));
    }

    @Override
    @Transactional
    public TicketPriorityResponse update(UpdateTicketPriorityRequest request) {
        TicketPriorityMaster priority = findPriority(request.priorityId());
        String code = normalizeCode(request.code());
        if (ticketPriorityMasterRepository.existsByCodeAndIdNot(code, priority.getId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Ticket priority code already exists");
        }

        priority.setName(request.name().trim());
        priority.setCode(code);
        priority.setDescription(request.description());
        if (request.status() != null) {
            priority.setStatus(request.status());
        }
        priority.setUpdatedAt(LocalDateTime.now());
        return toResponse(ticketPriorityMasterRepository.save(priority));
    }

    @Override
    @Transactional
    public TicketPriorityResponse delete(DeleteTicketPriorityRequest request) {
        TicketPriorityMaster priority = findPriority(request.priorityId());
        priority.setStatus(Status.DELETED);
        priority.setUpdatedAt(LocalDateTime.now());
        return toResponse(ticketPriorityMasterRepository.save(priority));
    }

    private TicketPriorityMaster findPriority(Long priorityId) {
        return ticketPriorityMasterRepository.findById(priorityId)
                .orElseThrow(() -> new NotFoundException("Ticket priority not found"));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private TicketPriorityResponse toResponse(TicketPriorityMaster priority) {
        return new TicketPriorityResponse(
                priority.getId(),
                priority.getName(),
                priority.getCode(),
                priority.getDescription(),
                priority.getStatus()
        );
    }
}
