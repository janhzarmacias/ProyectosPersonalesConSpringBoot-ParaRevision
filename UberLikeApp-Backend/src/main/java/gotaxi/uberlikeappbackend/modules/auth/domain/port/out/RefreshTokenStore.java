package gotaxi.uberlikeappbackend.modules.auth.domain.port.out;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.ActiveSessionResponse;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.RefreshTokenMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RefreshTokenStore {

    Mono<Void> storeRefreshToken(String token, RefreshTokenMetadata metadata);
    Mono<RefreshTokenMetadata> getMetadataFromRefreshToken(String token);
    Mono<Void> deleteRefreshToken(String token);

    Mono<Void> deleteByIdentifierAndDevice(String identifier, String deviceId, String userAgent);
    Mono<Void> deleteAllByIdentifier(String identifier);

    Mono<String> getRefreshTokenByIndex(String identifier, String deviceId, String userAgent);
    Mono<Void> deleteKeys(String... keys);

    Mono<List<ActiveSessionResponse>> getAllSessionsByIdentifier(String identifier);
    Flux<RefreshTokenMetadata> getAllMetadataByIdentifier(String identifier);


}
