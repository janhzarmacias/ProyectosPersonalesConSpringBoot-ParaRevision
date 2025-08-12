package ecommerce.ecommercevaldani.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmailServiceImp implements EmailService{

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Verifica tu cuenta";

        String verificationUrl = "http://localhost:3000/verify?token=" + token;

        String htmlContent = "<!DOCTYPE html>"
                + "<html lang='es'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<meta name='x-apple-disable-message-reformatting'>"
                + "<title>Verificación de cuenta Valdani</title>"
                + "<style>"
                + "  @media screen and (max-width: 600px) {"
                + "    .email-container { width: 100% !important; padding: 20px !important; }"
                + "    .btn { width: 100% !important; display: block !important; }"
                + "  }"
                + "</style>"
                + "</head>"
                + "<body style='margin:0; padding:0; background-color:#111827; font-family:Arial, sans-serif;'>"
                + "  <table role='presentation' width='100%' cellspacing='0' cellpadding='0' border='0' style='background-color:#111827;'>"
                + "    <tr><td align='center' style='padding: 30px 10px;'>"

                // Card principal
                + "      <table role='presentation' class='email-container' width='600' cellspacing='0' cellpadding='0' border='0'"
                + "        style='background-color:#1F2937; border-radius:12px; box-shadow:0 4px 16px rgba(0,0,0,0.6); overflow:hidden;'>"

                // Header
                + "        <tr><td style='padding: 40px 30px 20px; text-align: center;'>"
                + "          <h1 style='color:#F9FAFB; font-size: 24px; font-weight: bold;'>Bienvenido a <span style='color:#F9FAFB;'>Valdani</span></h1>"
                + "        </td></tr>"

                // Cuerpo
                + "        <tr><td style='padding: 0 30px 30px; text-align: center;'>"
                + "          <p style='color:#9CA3AF; font-size:16px; line-height:1.6;'>Hola 👋, gracias por registrarte.</p>"
                + "          <p style='color:#9CA3AF; font-size:16px;'>Para activar tu cuenta, haz clic en el botón:</p>"

                // Botón
                + "          <div style='margin: 30px 0;'>"
                + "            <a href='" + verificationUrl + "' target='_blank'"
                + "               style='background: linear-gradient(90deg, #4B5563, #6B7280); color: #F9FAFB; padding: 14px 32px;"
                + "               border-radius: 8px; text-decoration: none; font-weight: bold; font-size: 16px;'>"
                + "              Verificar mi cuenta</a>"
                + "          </div>"

                + "          <p style='color:#F9FAFB; font-size:14px;'>Este enlace expirará en 24 horas.</p>"
                + "        </td></tr>"

                // Footer interno
                + "        <tr><td style='padding: 20px 30px; text-align:center; background-color:#1F2937; border-top:1px solid #374151;'>"
                + "          <p style='color:#F9FAFB; font-size:13px;'>Si tú no creaste esta cuenta, puedes ignorar este mensaje.</p>"
                + "          <p style='color:#F9FAFB; font-size:13px;'>Para ayuda, contáctanos en <a href='mailto:valdaniverificacion@gmail.com' style='color:#9CA3AF; text-decoration:underline;'>valdaniverificacion@gmail.com</a></p>"
                + "        </td></tr>"

                + "      </table>"

                // Footer global
                + "      <table role='presentation' width='600' style='margin-top: 20px;'>"
                + "        <tr><td align='center' style='font-size: 12px; color: #6B7280;'>"
                + "          <p style='margin: 0;'>&copy; 2025 Valdani. Todos los derechos reservados.</p>"
                + "          <p style='margin: 8px;'>"
                + "            <a href='http://localhost:3000/terms' style='color:#9CA3AF; text-decoration:underline;'>Términos</a> | "
                + "            <a href='http://localhost:3000/privacy' style='color:#9CA3AF; text-decoration:underline;'>Privacidad</a>"
                + "          </p>"
                + "        </td></tr>"
                + "      </table>"

                + "    </td></tr>"
                + "  </table>"
                + "</body>"
                + "</html>";




        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true: HTML

            message.setHeader("X-Mailer", "MyMailer");
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setHeader("Content-Transfer-Encoding", "quoted-printable");

            message.setHeader("List-Unsubscribe", "<mailto:valdaniverificacion@gmail.com>"); // Reduce marca como spam
            message.setHeader("X-Entity-Ref-ID", UUID.randomUUID().toString()); // Evita filtros
            message.setHeader("Precedence", "bulk");

            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("No se pudo enviar el correo de verificación");
        }
    }

}
