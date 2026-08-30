package lk.di47.ticket.config;

import lk.di47.ticket.client.ai.AiPredictionClientProperties;
import lk.di47.ticket.crypto.CryptoProperties;
import lk.di47.ticket.feature.ticket.config.TicketAttachmentProperties;
import lk.di47.ticket.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@EnableConfigurationProperties({
        CryptoProperties.class,
        AiPredictionClientProperties.class,
        JwtProperties.class,
        TicketAttachmentProperties.class
})
public class AppConfig {
}
