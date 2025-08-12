package gotaxi.uberlikeappbackend.modules.auth.domain.port.in;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.ActiveSessionResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GetActiveSessionsUseCase {
    Mono<List<ActiveSessionResponse>> getSessionsFromToken(String accessToken);
    Mono<List<ActiveSessionResponse>> getSessions(String identifier);
}

