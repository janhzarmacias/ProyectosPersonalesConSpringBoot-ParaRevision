package gotaxi.uberlikeappbackend.modules.auth.api.dto.request;

public record LogoutRequest(String deviceId, String userAgent) {}