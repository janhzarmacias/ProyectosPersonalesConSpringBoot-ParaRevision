package gotaxi.uberlikeappbackend.modules.notification.infrastructure.config;

import gotaxi.uberlikeappbackend.modules.notification.infrastructure.handler.SessionEventHandler;
import gotaxi.uberlikeappbackend.modules.notification.domain.model.SessionEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocketConfig {

    @Bean
    public Sinks.Many<SessionEvent> sessionSink() {
        return Sinks.many().replay().limit(1);
    }

    @Bean
    public HandlerMapping webSocketMapping(SessionEventHandler handler) {
        Map<String, Object> map = new HashMap<>();
        map.put("/ws/session/stream", handler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(10); // prioridad de rutas
        mapping.setUrlMap(map);
        return mapping;
    }
}
