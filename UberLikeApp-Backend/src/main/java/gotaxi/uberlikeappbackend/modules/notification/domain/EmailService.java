package gotaxi.uberlikeappbackend.modules.notification.domain;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}