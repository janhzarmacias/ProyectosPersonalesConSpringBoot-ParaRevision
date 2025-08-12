package gotaxi.uberlikeappbackend.modules.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenMetadata {
    private String identifier;
    private String deviceId;
    private String userAgent;
    private LocalDateTime createdAt;

}
