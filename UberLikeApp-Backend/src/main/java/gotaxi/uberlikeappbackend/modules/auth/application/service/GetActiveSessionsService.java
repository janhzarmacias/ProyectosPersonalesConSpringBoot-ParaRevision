package gotaxi.uberlikeappbackend.modules.auth.application.service;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.ActiveSessionResponse;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.RefreshTokenMetadata;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.in.GetActiveSessionsUseCase;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.JwtDecoderPort;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetActiveSessionsService implements GetActiveSessionsUseCase {

    private final JwtDecoderPort jwtDecoderPort;
    private final RefreshTokenStore refreshTokenStore;

    @Override
    public Mono<List<ActiveSessionResponse>> getSessionsFromToken(String accessToken) {
        return jwtDecoderPort.decodeAccessToken(accessToken)
                .flatMap(payload -> getSessions(payload.identifier()));
    }

    @Override
    public Mono<List<ActiveSessionResponse>> getSessions(String identifier) {
        return refreshTokenStore.getAllMetadataByIdentifier(identifier)
                .map(this::mapToResponse)
                .collectList();
    }

    private ActiveSessionResponse mapToResponse(RefreshTokenMetadata metadata) {
        return new ActiveSessionResponse(
                metadata.getIdentifier(),
                metadata.getDeviceId(),
                metadata.getUserAgent(),
                metadata.getCreatedAt()
        );
    }
}
