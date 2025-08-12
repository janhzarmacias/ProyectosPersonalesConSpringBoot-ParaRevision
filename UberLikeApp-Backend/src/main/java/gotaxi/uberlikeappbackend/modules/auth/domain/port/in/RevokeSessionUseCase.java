package gotaxi.uberlikeappbackend.modules.auth.domain.port.in;

import reactor.core.publisher.Mono;

public interface RevokeSessionUseCase {
    Mono<Void> revokeSessionFromJwt(String jwt, String deviceId, String userAgent);
}
