package lk.di47.ticket.feature.ticketstatus.service.impl;

import lk.di47.ticket.entity.TicketStatusMaster;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.ticketstatus.dto.*;
import lk.di47.ticket.feature.ticketstatus.service.TicketStatusMasterService;
import lk.di47.ticket.repository.TicketStatusMasterRepository;
import lk.di47.ticket.util.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketStatusMasterServiceImpl implements TicketStatusMasterService {
    private final TicketStatusMasterRepository ticketStatusMasterRepository;

    @Override
    @Transactional
    public TicketStatusMasterResponse create(CreateTicketStatusRequest request) {
        if (ticketStatusMasterRepository.existsByCode(request.code())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Ticket status code already exists");
        }

        TicketStatusMaster status = new TicketStatusMaster();
        status.setName(request.name());
        status.setCode(request.code());
        status.setDescription(request.description());
        status.setStatus(Status.ACTIVE);
        status.setCreatedAt(LocalDateTime.now());
        return toResponse(ticketStatusMasterRepository.save(status));
    }

    @Override
    public List<TicketStatusMasterResponse> list() {
        return ticketStatusMasterRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public TicketStatusMasterResponse detail(TicketStatusDetailRequest request) {
        return toResponse(findStatus(request.statusId()));
    }

    @Override
    @Transactional
    public TicketStatusMasterResponse update(UpdateTicketStatusMasterRequest request) {
        TicketStatusMaster status = findStatus(request.statusId());
        if (ticketStatusMasterRepository.existsByCodeAndIdNot(request.code(), status.getId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Ticket status code already exists");
        }

        status.setName(request.name());
        status.setCode(request.code());
        status.setDescription(request.description());
        if (request.status() != null) {
            status.setStatus(request.status());
        }
        status.setUpdatedAt(LocalDateTime.now());
        return toResponse(ticketStatusMasterRepository.save(status));
    }

    private TicketStatusMaster findStatus(Long statusId) {
        return ticketStatusMasterRepository.findById(statusId)
                .orElseThrow(() -> new NotFoundException("Ticket status not found"));
    }

    private TicketStatusMasterResponse toResponse(TicketStatusMaster status) {
        return new TicketStatusMasterResponse(
                status.getId(),
                status.getName(),
                status.getCode(),
                status.getDescription(),
                status.getStatus()
        );
    }
}
