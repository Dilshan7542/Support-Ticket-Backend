package lk.di47.ticket.util.mask;



import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;


import java.util.Set;

public final class SensitiveDataMasker {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "otp", "token", "accessToken", "refreshToken", "authorization", "nic", "phone", "email"
    );

    private SensitiveDataMasker() {}

    public static String mask(String body) {
        if (body == null || body.isBlank()) {
            return body;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(body);
            maskNode(root);
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (Exception ignored) {
            return body.length() > 4000 ? body.substring(0, 4000) : body;
        }
    }

    private static void maskNode(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.propertyNames().forEach(field -> {
                if (SENSITIVE_FIELDS.contains(field)) {
                    objectNode.put(field, "****");
                } else {
                    maskNode(objectNode.get(field));
                }
            });
        } else if (node.isArray()) {
            node.forEach(SensitiveDataMasker::maskNode);
        }
    }
}
