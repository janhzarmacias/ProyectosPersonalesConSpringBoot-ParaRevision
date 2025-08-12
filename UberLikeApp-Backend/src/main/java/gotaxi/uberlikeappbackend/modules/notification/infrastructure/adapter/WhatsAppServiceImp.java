package gotaxi.uberlikeappbackend.modules.notification.infrastructure.adapter;

import gotaxi.uberlikeappbackend.modules.notification.domain.WhatsAppService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppServiceImp implements WhatsAppService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.whatsapp.from-number}")
    private String twilioWhatsAppFromNumber;

    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
    }


    @Override
    @Async
    public void sendWhatsAppMessage(String toPhoneNumber, String message) { // <-- MÉTODO MODIFICADO/NUEVO
        try {
            String formattedToPhoneNumber = "whatsapp:" + toPhoneNumber;

            Message.creator(
                    new PhoneNumber(formattedToPhoneNumber),
                    new PhoneNumber(twilioWhatsAppFromNumber),
                    message
            ).create();

            System.out.println("DEBUG: WhatsApp message sent successfully to " + toPhoneNumber + ".");

        } catch (Exception e) {
            System.err.println("ERROR: Fallo al enviar mensaje de WhatsApp a " + toPhoneNumber + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}