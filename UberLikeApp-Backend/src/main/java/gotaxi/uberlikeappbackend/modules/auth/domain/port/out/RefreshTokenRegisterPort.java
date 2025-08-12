package gotaxi.uberlikeappbackend.modules.auth.domain.port.out;

import gotaxi.uberlikeappbackend.modules.auth.domain.model.RefreshTokenMetadata;
import reactor.core.publisher.Mono;

public interface RefreshTokenRegisterPort {
    Mono<String> registerRefreshToken(RefreshTokenMetadata metadata);
    Mono<String> getRefreshTokenByIndex(String identifier, String deviceId, String userAgent);
    Mono<Void> deleteKeys(String... keys);
}