package gotaxi.uberlikeappbackend.modules.notification.infrastructure.redis;

import gotaxi.uberlikeappbackend.modules.notification.domain.model.SessionEvent;
import gotaxi.uberlikeappbackend.modules.notification.domain.port.in.SessionEventReceiverPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisSessionEventListener {

    private final RedisConnectionFactory redisConnectionFactory;
    private final SessionEventReceiverPort receiver;

    @Bean
    public RedisMessageListenerContainer redisContainer(MessageListenerAdapter messageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("session-events"));
        return container;
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter() {
        MessageListenerAdapter adapter = new MessageListenerAdapter(this, "handleMessage");
        adapter.setSerializer(new Jackson2JsonRedisSerializer<>(SessionEvent.class));
        return adapter;
    }

    // Este es el metodo que será invocado por el listener
    public void handleMessage(SessionEvent event) {
        log.info("📥 Evento recibido desde Redis: {}", event);
        receiver.onSessionEvent(event).subscribe(); // reemite al WebSocket
    }
}
