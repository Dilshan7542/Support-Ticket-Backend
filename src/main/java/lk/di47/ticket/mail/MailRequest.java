package lk.di47.ticket.mail;

public record MailRequest(
        String to,
        String subject,
        String body
) {
}
