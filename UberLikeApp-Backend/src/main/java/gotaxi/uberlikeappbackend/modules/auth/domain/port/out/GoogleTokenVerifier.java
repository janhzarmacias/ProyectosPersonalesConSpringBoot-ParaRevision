package gotaxi.uberlikeappbackend.modules.auth.domain.port.out;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.external.GoogleUserInfo;
import reactor.core.publisher.Mono;

public interface GoogleTokenVerifier {
    Mono<GoogleUserInfo> verify(String idToken);
}