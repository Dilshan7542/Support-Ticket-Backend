package lk.di47.ticket.util.generator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class TicketNumberGenerator {
    private TicketNumberGenerator() {}

    public static String generate() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TKT-" + date + "-" + random;
    }
}
