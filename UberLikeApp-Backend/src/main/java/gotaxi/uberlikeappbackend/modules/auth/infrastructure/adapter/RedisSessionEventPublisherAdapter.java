package gotaxi.uberlikeappbackend.modules.auth.infrastructure.adapter;

import gotaxi.uberlikeappbackend.modules.auth.domain.model.SessionEvent;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.SessionEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RedisSessionEventPublisherAdapter implements SessionEventPublisherPort {

    private final ReactiveStringRedisTemplate redisTemplate;
    private static final String CHANNEL = "session-events";

    @Override
    public Mono<Void> publishSessionEvent(SessionEvent event) {
        String message = serializeEvent(event);
        return redisTemplate.convertAndSend(CHANNEL, message).then();
    }

    private String serializeEvent(SessionEvent event) {
        return String.format("{\"type\":%s,\"identifier\":\"%s\",%s}",
                event.getType() == null ? "null" : "\"" + event.getType() + "\"",
                event.getIdentifier(),
                event.getDeviceId() == null ? "\"deviceId\":null" : "\"deviceId\":\"" + event.getDeviceId() + "\""
        );
    }
}
