package lk.di47.ticket.util.date;

import java.time.LocalDateTime;

public final class DateTimeUtil {
    private DateTimeUtil() {}

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}
