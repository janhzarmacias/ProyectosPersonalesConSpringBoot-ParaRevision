package gotaxi.uberlikeappbackend.modules.notification.domain.model;

import java.time.Instant;

public record SessionEvent(
        String type,
        String identifier,
        String deviceId,
        Instant timestamp
) {
    public SessionEvent(String type, String identifier, String deviceId) {
        this(type, identifier, deviceId, Instant.now());
    }
}
