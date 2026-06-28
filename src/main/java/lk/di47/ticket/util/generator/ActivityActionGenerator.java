package lk.di47.ticket.util.generator;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ActivityActionGenerator {
    private static final Set<String> IGNORED_SEGMENTS = Set.of("api", "v1", "v2");

    private ActivityActionGenerator() {}

    public static String generate(String path) {
        if (path == null || path.isBlank()) {
            return "UNKNOWN";
        }

        List<String> segments = Arrays.stream(path.split("/"))
                .filter(segment -> !segment.isBlank())
                .filter(segment -> !IGNORED_SEGMENTS.contains(segment.toLowerCase()))
                .filter(segment -> !isNumeric(segment))
                .filter(segment -> !isUuid(segment))
                .toList();

        if (segments.isEmpty()) {
            return "UNKNOWN";
        }

        int fromIndex = Math.max(segments.size() - 2, 0);
        return segments.subList(fromIndex, segments.size())
                .stream()
                .map(segment -> segment.replace("-", "_").toUpperCase())
                .collect(Collectors.joining("_"));
    }

    private static boolean isNumeric(String value) {
        return value.chars().allMatch(Character::isDigit);
    }

    private static boolean isUuid(String value) {
        return value.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }
}
