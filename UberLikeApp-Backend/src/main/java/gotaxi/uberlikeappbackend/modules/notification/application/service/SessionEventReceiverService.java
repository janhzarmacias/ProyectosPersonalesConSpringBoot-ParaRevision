package gotaxi.uberlikeappbackend.modules.notification.application.service;

import gotaxi.uberlikeappbackend.modules.notification.domain.model.SessionEvent;
import gotaxi.uberlikeappbackend.modules.notification.domain.port.in.SessionEventReceiverPort;
import gotaxi.uberlikeappbackend.modules.notification.infrastructure.websocket.WebSocketSessionEmitter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SessionEventReceiverService implements SessionEventReceiverPort {

    private final WebSocketSessionEmitter emitter;

    @Override
    public Mono<Void> onSessionEvent(SessionEvent event) {
        return emitter.emit(event);
    }
}
