package gotaxi.uberlikeappbackend.modules.auth.application.service;

import gotaxi.uberlikeappbackend.modules.auth.domain.model.SessionEvent;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.in.RevokeSessionUseCase;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.JwtDecoderPort;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.RefreshTokenStore;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.SessionEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RevokeSessionService implements RevokeSessionUseCase {

    private final JwtDecoderPort jwtDecoderPort;
    private final RefreshTokenStore refreshTokenStore;
    private final SessionEventPublisherPort sessionEventPublisherPort;

    @Override
    public Mono<Void> revokeSessionFromJwt(String jwt, String deviceId, String userAgent) {
        return jwtDecoderPort.decodeAccessToken(jwt)
                .flatMap(payload -> refreshTokenStore
                        .deleteByIdentifierAndDevice(payload.identifier(), deviceId, userAgent)
                        .then(sessionEventPublisherPort.publishSessionEvent(
                                new SessionEvent("session_revoked", payload.identifier(), deviceId)
                        ))
                        .then(Mono.delay(Duration.ofMillis(100))) // si aún deseas emitir neutral
                        .then(sessionEventPublisherPort.publishSessionEvent(
                                new SessionEvent(null, payload.identifier(), deviceId)
                        ))
                );
    }

}


