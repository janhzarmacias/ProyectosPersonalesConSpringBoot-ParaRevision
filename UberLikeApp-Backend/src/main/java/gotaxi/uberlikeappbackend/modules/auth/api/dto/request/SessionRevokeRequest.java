package gotaxi.uberlikeappbackend.modules.auth.api.dto.request;

public record SessionRevokeRequest(
        String deviceId,
        String userAgent
) {}