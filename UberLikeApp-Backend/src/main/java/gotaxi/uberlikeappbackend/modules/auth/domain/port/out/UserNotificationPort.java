package gotaxi.uberlikeappbackend.modules.auth.domain.port.out;

public interface UserNotificationPort {
    void sendVerificationEmail(String email, String code);
    void sendVerificationWhatsApp(String phoneNumber, String code);
    void sendPasswordResetCode(String identifier, String code);
}