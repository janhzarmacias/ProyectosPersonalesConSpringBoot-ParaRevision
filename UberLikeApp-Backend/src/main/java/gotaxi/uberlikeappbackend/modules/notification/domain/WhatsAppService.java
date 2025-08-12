package gotaxi.uberlikeappbackend.modules.notification.domain;

public interface WhatsAppService {
    void sendWhatsAppMessage(String toPhoneNumber, String message);
}