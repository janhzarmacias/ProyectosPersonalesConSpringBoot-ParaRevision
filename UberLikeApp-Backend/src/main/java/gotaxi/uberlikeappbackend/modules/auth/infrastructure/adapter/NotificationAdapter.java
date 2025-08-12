package gotaxi.uberlikeappbackend.modules.auth.infrastructure.adapter;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.UserNotificationPort;
import gotaxi.uberlikeappbackend.modules.notification.domain.EmailService;
import gotaxi.uberlikeappbackend.modules.notification.domain.WhatsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class NotificationAdapter implements UserNotificationPort {
    private final EmailService emailService;
    private final WhatsAppService whatsAppService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    @Override
    public void sendVerificationEmail(String email, String code) {
        String subject = "Verificación de Cuenta GoTaxi";
        String body = "¡Bienvenido a GoTaxi!\n\nTu código de verificación de cuenta es: " + code + "\n\nEste código expirará en 2 minutos. Si no solicitaste esto, ignora este correo.";
        emailService.sendEmail(email, subject, body); // Llama al nuevo método general
    }

    @Override
    public void sendVerificationWhatsApp(String phoneNumber, String code) {
        if (!isValidPhoneNumber(phoneNumber)) {
            System.err.println("ERROR: WhatsApp number format invalid for verification: " + phoneNumber);
            return;
        }
        String message = "¡Bienvenido a GoTaxi! Tu código de verificación de cuenta es: *" + code + "*. Este código expira en 2 minutos. Si no lo solicitaste, ignora este mensaje.";
        whatsAppService.sendWhatsAppMessage(phoneNumber, message); // Llama al nuevo método general
    }

    @Override
    public void sendPasswordResetCode(String identifier, String code) {
        if (EMAIL_PATTERN.matcher(identifier).matches()) {
            String subject = "Recuperación de Contraseña GoTaxi";
            String body = "Has solicitado restablecer tu contraseña para GoTaxi.\n\nTu código de recuperación es: " + code + "\n\nEste código es válido por 10 minutos. Por favor, no compartas este código con nadie.\n\nSi no solicitaste este cambio, por favor ignora este correo.";
            emailService.sendEmail(identifier, subject, body); // Llama al nuevo método general
        } else {
            String message = "GoTaxi: Tu código de recuperación de contraseña es: *" + code + "*. Este código es válido por 10 minutos. Por favor, no lo compartas con nadie.";
            whatsAppService.sendWhatsAppMessage(identifier, message); // Llama al nuevo método general
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        try {
            Phonenumber.PhoneNumber number = phoneNumberUtil.parse(phoneNumber, "ZZ"); // "ZZ" for unknown region
            return phoneNumberUtil.isValidNumber(number);
        } catch (com.google.i18n.phonenumbers.NumberParseException e) {
            System.err.println("DEBUG: Failed to parse phone number " + phoneNumber + ": " + e.getMessage());
            return false;
        }
    }
}