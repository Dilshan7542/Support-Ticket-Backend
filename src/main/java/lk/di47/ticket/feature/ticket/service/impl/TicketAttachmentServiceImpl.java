package lk.di47.ticket.feature.ticket.service.impl;

import lk.di47.ticket.entity.Ticket;
import lk.di47.ticket.entity.TicketAttachment;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.ticket.config.TicketAttachmentProperties;
import lk.di47.ticket.feature.ticket.dto.TicketAttachmentDownload;
import lk.di47.ticket.feature.ticket.dto.TicketAttachmentResponse;
import lk.di47.ticket.feature.ticket.service.TicketAttachmentService;
import lk.di47.ticket.repository.TicketAttachmentRepository;
import lk.di47.ticket.repository.TicketRepository;
import lk.di47.ticket.repository.UserRepository;
import lk.di47.ticket.util.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketAttachmentServiceImpl implements TicketAttachmentService {
    private final TicketAttachmentRepository ticketAttachmentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketAttachmentProperties attachmentProperties;

    @Override
    @Transactional
    public TicketAttachmentResponse uploadAttachment(Long userId, Long ticketId, MultipartFile file) {
        validateAttachmentRequest(userId, ticketId, file);

        String originalFileName = sanitizeFileName(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + resolveExtension(originalFileName);
        Path targetDirectory = resolveTargetDirectory(ticketId, userId);
        Path targetPath = targetDirectory.resolve(storedFileName);

        try {
            Files.createDirectories(targetDirectory);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Unable to save attachment", exception);
        }

        TicketAttachment attachment = new TicketAttachment();
        attachment.setTicketId(ticketId);
        attachment.setUploadedByUserId(userId);
        attachment.setOriginalFileName(originalFileName);
        attachment.setStoredFileName(storedFileName);
        attachment.setContentType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setStoragePath(targetPath.toString());
        attachment.setCreatedAt(LocalDateTime.now());
        return toAttachmentResponse(ticketAttachmentRepository.save(attachment));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketAttachmentDownload downloadAttachment(Long attachmentId, Long requesterUserId) {
        if (attachmentId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "attachmentId is required");
        }
        if (requesterUserId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Missing authenticated user");
        }
        TicketAttachment attachment = ticketAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("Attachment not found"));
        validateAttachmentDownloadAccess(requesterUserId, attachment);

        Path basePath = Path.of(attachmentProperties.getStoragePath()).toAbsolutePath().normalize();
        Path attachmentPath = Path.of(attachment.getStoragePath()).toAbsolutePath().normalize();
        if (!attachmentPath.startsWith(basePath)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Invalid attachment path");
        }
        if (!Files.exists(attachmentPath) || !Files.isRegularFile(attachmentPath)) {
            throw new NotFoundException("Attachment file not found");
        }

        Resource resource = new FileSystemResource(attachmentPath);
        return new TicketAttachmentDownload(
                attachment.getId(),
                resource,
                attachment.getOriginalFileName(),
                resolveContentType(attachment),
                attachment.getFileSize()
        );
    }

    private void validateAttachmentRequest(Long userId, Long ticketId, MultipartFile file) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "userId is required");
        }
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
        if (ticketId != null) {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new NotFoundException("Ticket not found"));
            validateCustomerTicketAccess(userId, ticket);
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "attachment is required");
        }
        if (file.getSize() > attachmentProperties.getMaxFileSizeBytes()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Attachment exceeds max file size");
        }
    }

    private void validateAttachmentDownloadAccess(Long requesterUserId, TicketAttachment attachment) {
        if (resolveUserRole(requesterUserId) != UserRole.CUSTOMER) {
            return;
        }
        if (attachment.getTicketId() == null) {
            if (!requesterUserId.equals(attachment.getUploadedByUserId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "You do not have access to this attachment");
            }
            return;
        }
        Ticket ticket = ticketRepository.findById(attachment.getTicketId())
                .orElseThrow(() -> new NotFoundException("Ticket not found"));
        validateCustomerTicketAccess(requesterUserId, ticket);
    }

    private void validateCustomerTicketAccess(Long userId, Ticket ticket) {
        if (resolveUserRole(userId) == UserRole.CUSTOMER && !userId.equals(ticket.getCustomerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "You do not have access to this ticket");
        }
    }

    private UserRole resolveUserRole(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"))
                .getRole();
    }

    private Path resolveTargetDirectory(Long ticketId, Long userId) {
        Path basePath = Path.of(attachmentProperties.getStoragePath()).toAbsolutePath().normalize();
        if (ticketId != null) {
            return basePath.resolve("tickets").resolve(String.valueOf(ticketId)).normalize();
        }
        return basePath.resolve("users").resolve(String.valueOf(userId)).normalize();
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "attachment";
        }
        String sanitized = Path.of(fileName).getFileName().toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        return sanitized.isBlank() ? "attachment" : sanitized;
    }

    private String resolveExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    private String resolveContentType(TicketAttachment attachment) {
        if (attachment.getContentType() == null || attachment.getContentType().isBlank()) {
            return "application/octet-stream";
        }
        return attachment.getContentType();
    }

    private TicketAttachmentResponse toAttachmentResponse(TicketAttachment attachment) {
        return new TicketAttachmentResponse(
                attachment.getId(),
                attachment.getTicketId(),
                attachment.getUploadedByUserId(),
                attachment.getOriginalFileName(),
                attachment.getStoredFileName(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getStoragePath(),
                attachment.getCreatedAt()
        );
    }
}
