package gotaxi.uberlikeappbackend.modules.notification.infrastructure.pubsub;

import gotaxi.uberlikeappbackend.modules.notification.domain.model.SessionEvent;
import gotaxi.uberlikeappbackend.modules.notification.domain.port.in.SessionEventReceiverPort;
import gotaxi.uberlikeappbackend.modules.notification.infrastructure.websocket.WebSocketSessionEmitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisSessionEventSubscriberAdapter implements MessageListener {

    private final WebSocketSessionEmitter emitter;
    private final SessionEventReceiverPort receiverPort;

    private final Jackson2JsonRedisSerializer<SessionEvent> serializer = new Jackson2JsonRedisSerializer<>(SessionEvent.class);

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            SessionEvent event = serializer.deserialize(message.getBody());
            if (event == null) return;

            log.info("📩 [RedisSubscriber] Evento recibido: {}", event);
            // Delegamos la lógica al puerto de entrada (y éste usará el WebSocket)
            receiverPort.onSessionEvent(event)
                    .doOnError(err -> log.error("❌ Error procesando evento WebSocket: {}", err.getMessage()))
                    .subscribe();

        } catch (Exception e) {
            log.error("❌ Error deserializando mensaje Redis: {}", e.getMessage(), e);
        }
    }
}
