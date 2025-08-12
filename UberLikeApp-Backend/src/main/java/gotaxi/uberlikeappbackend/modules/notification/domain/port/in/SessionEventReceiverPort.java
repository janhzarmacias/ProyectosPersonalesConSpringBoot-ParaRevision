package gotaxi.uberlikeappbackend.modules.notification.domain.port.in;

import gotaxi.uberlikeappbackend.modules.notification.domain.model.SessionEvent;
import reactor.core.publisher.Mono;

public interface SessionEventReceiverPort {
    Mono<Void> onSessionEvent(SessionEvent event);
}
