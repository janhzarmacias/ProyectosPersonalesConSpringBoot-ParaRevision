package gotaxi.uberlikeappbackend.modules.auth.api.dto.response;

import java.time.LocalDateTime;

public record ActiveSessionResponse(
        String identifier,
        String deviceId,
        String userAgent,
        LocalDateTime createdAt
) {}

