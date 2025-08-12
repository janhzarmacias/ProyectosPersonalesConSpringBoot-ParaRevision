package gotaxi.uberlikeappbackend.modules.auth.domain.port.in;

import reactor.core.publisher.Mono;

public interface LogoutUseCase {
    Mono<Void> logout(String refreshToken, String deviceId, String userAgent);
    Mono<Void> logoutAll(String accessToken);
}
