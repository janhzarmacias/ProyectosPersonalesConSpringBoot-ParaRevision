package gotaxi.uberlikeappbackend.modules.auth.domain.port.out;

import gotaxi.uberlikeappbackend.modules.auth.domain.model.JwtPayload;
import reactor.core.publisher.Mono;

public interface JwtDecoderPort {
    Mono<JwtPayload> decodeRefreshToken(String token);
    Mono<JwtPayload> decodeAccessToken(String token);
}
