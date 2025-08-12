package gotaxi.uberlikeappbackend.modules.notification.infrastructure.adapter;

import gotaxi.uberlikeappbackend.modules.notification.domain.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class EmailServiceImp implements EmailService {

    private final WebClient webClient;
    private final String mailgunDomain;

    public EmailServiceImp(
            @Value("${mailgun.api.key}") String mailgunApiKey,
            @Value("${mailgun.api.domain}") String mailgunDomain,
            @Value("${mailgun.api.url}") String mailgunApiUrl) {

        this.mailgunDomain = mailgunDomain;
        this.webClient = WebClient.builder()
                .baseUrl(mailgunApiUrl + "/" + this.mailgunDomain + "/messages")
                .defaultHeaders(headers -> headers.setBasicAuth("api", mailgunApiKey))
                .build();
    }

    @Override
    @Async
    public void sendEmail(String toEmail, String subject, String body) { // <-- MÉTODO MODIFICADO/NUEVO
        String fromEmail = "GoTaxi <noreply@" + this.mailgunDomain + ">";

        Mono<String> mailgunResponse = webClient.post()
                .body(BodyInserters.fromFormData("from", fromEmail)
                        .with("to", toEmail)
                        .with("subject", subject)
                        .with("text", body))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Mailgun API Error (" + clientResponse.statusCode() + "): " + errorBody))))
                .bodyToMono(String.class)
                .doOnSuccess(response -> System.out.println("DEBUG: Correo Mailgun enviado con éxito para " + toEmail + ". Respuesta: " + response))
                .doOnError(e -> System.err.println("ERROR: Fallo al enviar correo con Mailgun para " + toEmail + ": " + e.getMessage()));

        mailgunResponse.subscribe();
    }
}