package gotaxi.uberlikeappbackend.modules.notification.infrastructure.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.JwtDecoderPort;
import gotaxi.uberlikeappbackend.modules.notification.domain.model.SessionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class SessionEventHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SessionEventHandler.class);

    private final Sinks.Many<SessionEvent> sessionSink;
    private final ObjectMapper objectMapper;
    private final JwtDecoderPort jwtDecoderPort;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String deviceId = extractDeviceId(session);
        if (deviceId == null || deviceId.isBlank()) {
            return session.send(Mono.just(session.textMessage("{\"error\":\"missing_device_id\"}")));
        }

        String token = extractBearerToken(session);
        if (token == null || token.isBlank()) {
            return session.send(Mono.just(session.textMessage("{\"error\":\"unauthorized\"}")));
        }

        return jwtDecoderPort.decodeAccessToken(token)
                .flatMap(payload -> {
                    String identifier = payload.identifier();

                    log.info("🟢 WebSocket conectado: identifier={}, deviceId={}", identifier, deviceId);

                    var eventStream = sessionSink.asFlux()
                            .filter(event ->
                                    event.identifier().equals(identifier) &&
                                            (event.deviceId() == null || event.deviceId().equals(deviceId))
                            )
                            .doOnSubscribe(sub -> log.info("📡 Subscribed to sessionSink for: {}", identifier))
                            .doOnNext(event -> log.info("📤 Enviando evento a cliente WebSocket: {}", event))
                            .map(event -> {
                                try {
                                    String json = objectMapper.writeValueAsString(event);
                                    return session.textMessage(json);
                                } catch (Exception e) {
                                    log.error("❌ Error serializando evento", e);
                                    return session.textMessage("{\"error\":\"serialization\"}");
                                }
                            });

                    // 👇 combinación: escucha entrada y salida para que la conexión se mantenga viva
                    return session.send(eventStream)
                            .and(session.receive().doOnError(e ->
                                    log.warn("🔴 Error en WebSocket receive: {}", e.getMessage())
                            ).doOnComplete(() ->
                                    log.info("🔴 Cliente cerró conexión WebSocket: identifier={}, deviceId={}", identifier, deviceId)
                            ).then())
                            .doFinally(signal -> log.info("🔴 WebSocket desconectado: identifier={}, deviceId={}, motivo={}", identifier, deviceId, signal));
                })
                .onErrorResume(e -> {
                    log.warn("❌ Error al validar token en WebSocket: {}", e.getMessage());
                    return session.send(Mono.just(session.textMessage("{\"error\":\"invalid_token\"}")));
                });
    }

    private String extractDeviceId(WebSocketSession session) {
        String query = session.getHandshakeInfo().getUri().getQuery();
        if (query != null && query.contains("deviceId=")) {
            return query.replaceAll(".*deviceId=([^&]+).*", "$1");
        }
        return null;
    }

    private String extractBearerToken(WebSocketSession session) {
        String authHeader = session.getHandshakeInfo().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

