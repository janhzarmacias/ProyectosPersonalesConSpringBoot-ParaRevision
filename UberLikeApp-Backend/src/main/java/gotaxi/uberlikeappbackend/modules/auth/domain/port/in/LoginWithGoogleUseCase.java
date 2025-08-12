package gotaxi.uberlikeappbackend.modules.auth.domain.port.in;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.request.GoogleTokenRequest;
import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.JwtTokensResponse;
import reactor.core.publisher.Mono;

public interface LoginWithGoogleUseCase {
    Mono<JwtTokensResponse> loginWithGoogle(GoogleTokenRequest request);
}
