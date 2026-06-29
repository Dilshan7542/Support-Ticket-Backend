package lk.di47.ticket.feature.ticket.service.impl;

import lk.di47.ticket.entity.TicketAttachment;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.ticket.config.TicketAttachmentProperties;
import lk.di47.ticket.feature.ticket.dto.TicketAttachmentResponse;
import lk.di47.ticket.feature.ticket.service.TicketAttachmentService;
import lk.di47.ticket.repository.TicketAttachmentRepository;
import lk.di47.ticket.repository.TicketRepository;
import lk.di47.ticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Unable to save attachment");
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

    private void validateAttachmentRequest(Long userId, Long ticketId, MultipartFile file) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "userId is required");
        }
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
        if (ticketId != null && !ticketRepository.existsById(ticketId)) {
            throw new NotFoundException("Ticket not found");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "attachment is required");
        }
        if (file.getSize() > attachmentProperties.getMaxFileSizeBytes()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Attachment exceeds max file size");
        }
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
