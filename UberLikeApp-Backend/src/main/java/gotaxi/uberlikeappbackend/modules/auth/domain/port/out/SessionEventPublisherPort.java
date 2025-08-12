package gotaxi.uberlikeappbackend.modules.auth.domain.port.out;

import gotaxi.uberlikeappbackend.modules.auth.domain.model.SessionEvent;
import reactor.core.publisher.Mono;

public interface SessionEventPublisherPort {
    Mono<Void> publishSessionEvent(SessionEvent event);
}
