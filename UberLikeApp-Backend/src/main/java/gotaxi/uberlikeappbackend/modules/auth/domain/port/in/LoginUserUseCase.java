package gotaxi.uberlikeappbackend.modules.auth.domain.port.in;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.JwtTokensResponse;
import reactor.core.publisher.Mono;

public interface LoginUserUseCase {
    Mono<JwtTokensResponse> loginLocal(String identifier, String rawPassword, String userAgent, String deviceId);
}