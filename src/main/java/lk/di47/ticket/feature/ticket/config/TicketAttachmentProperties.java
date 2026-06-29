package lk.di47.ticket.feature.ticket.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.attachment")
public class TicketAttachmentProperties {
    private String storagePath = "D:/support-ticket-attachments";
    private long maxFileSizeBytes = 10 * 1024 * 1024;
}
