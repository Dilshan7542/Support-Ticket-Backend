package lk.di47.ticket.crypto;

import java.util.Locale;

public final class EncryptionAadFactory {
    private static final String PROTOCOL_VERSION = "AI-TICKET-V4";

    private EncryptionAadFactory() {
    }

    public static String requestAad(String keyId, String method, String path, String timestamp, String nonce) {
        return String.join("|",
                PROTOCOL_VERSION,
                "REQUEST",
                keyId,
                method.toUpperCase(Locale.ROOT),
                path,
                timestamp,
                nonce
        );
    }

    public static String responseAad(String keyId, int status, String timestamp, String nonce) {
        return String.join("|",
                PROTOCOL_VERSION,
                "RESPONSE",
                keyId,
                String.valueOf(status),
                timestamp,
                nonce
        );
    }
}
