package gotaxi.uberlikeappbackend.modules.notification.infrastructure.websocket;

import gotaxi.uberlikeappbackend.modules.notification.domain.model.SessionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class WebSocketSessionEmitter {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionEmitter.class);

    private final Sinks.Many<SessionEvent> sessionSink;

    public Mono<Void> emit(SessionEvent event) {
        var result = sessionSink.tryEmitNext(event);
        if (result.isFailure()) {
            log.error("❌ Fallo al emitir evento WebSocket: {} → Resultado: {}", event, result);
        } else {
            log.info("✅ Evento emitido al WebSocketSink: {}", event);
        }
        return Mono.empty();
    }
}
