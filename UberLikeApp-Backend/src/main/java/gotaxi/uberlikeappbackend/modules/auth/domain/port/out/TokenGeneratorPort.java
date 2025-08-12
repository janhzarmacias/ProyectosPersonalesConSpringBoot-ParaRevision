package gotaxi.uberlikeappbackend.modules.auth.domain.port.out;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.JwtTokensResponse;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.UserAuth;
import reactor.core.publisher.Mono;

public interface TokenGeneratorPort {
    Mono<JwtTokensResponse> generateTokens(UserAuth user, String userAgent, String deviceId);
}