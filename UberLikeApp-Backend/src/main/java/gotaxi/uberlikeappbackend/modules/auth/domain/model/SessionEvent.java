package gotaxi.uberlikeappbackend.modules.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionEvent {
    private String type;
    private String identifier;
    private String deviceId;
}