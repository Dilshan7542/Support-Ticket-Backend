package lk.di47.ticket.client.ai;

import lk.di47.ticket.feature.ai.dto.AiPredictionRequest;
import lk.di47.ticket.feature.ai.dto.AiPredictionResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "mode", havingValue = "java", matchIfMissing = true)
public class JavaAiPredictionClient implements AiPredictionGateway {

    @Override
    public AiPredictionResponse predict(AiPredictionRequest request) {
        String description = request.description() == null ? "" : request.description().toLowerCase(Locale.ROOT);
        return new AiPredictionResponse(
                resolveCategory(description),
                resolvePriority(description),
                null,
                0.60
        );
    }

    private String resolveCategory(String description) {
        if (containsAny(description, "login", "password", "otp", "token", "account", "access")) {
            return "AUTH";
        }
        if (containsAny(description, "payment", "invoice", "refund", "billing", "card")) {
            return "BILLING";
        }
        if (containsAny(description, "bug", "error", "exception", "crash", "failed", "not working")) {
            return "TECHNICAL";
        }
        if (containsAny(description, "slow", "timeout", "performance", "delay")) {
            return "PERFORMANCE";
        }
        return "GENERAL";
    }

    private String resolvePriority(String description) {
        if (containsAny(description, "critical", "urgent", "production down", "cannot login", "data loss", "security")) {
            return "CRITICAL";
        }
        if (containsAny(description, "blocked", "failed", "crash", "exception", "not working", "payment")) {
            return "HIGH";
        }
        if (containsAny(description, "slow", "delay", "issue", "problem", "error")) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
