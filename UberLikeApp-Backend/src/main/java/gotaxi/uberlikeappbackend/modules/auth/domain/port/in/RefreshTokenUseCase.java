package gotaxi.uberlikeappbackend.modules.auth.domain.port.in;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.JwtTokensResponse;
import reactor.core.publisher.Mono;

public interface RefreshTokenUseCase {
    Mono<JwtTokensResponse> refresh(String refreshToken, String userAgent, String deviceId);
}
