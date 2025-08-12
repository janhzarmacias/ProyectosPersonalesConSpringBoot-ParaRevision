package gotaxi.uberlikeappbackend.modules.auth.application.service;

import gotaxi.uberlikeappbackend.modules.auth.domain.model.SessionEvent;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.in.LogoutUseCase;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.JwtDecoderPort;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.RefreshTokenStore;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.SessionEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenStore refreshTokenStore;
    private final JwtDecoderPort jwtDecoderPort;
    private final SessionEventPublisherPort sessionEventPublisherPort;

    @Override
    public Mono<Void> logoutAll(String accessToken) {
        return jwtDecoderPort.decodeAccessToken(accessToken)
                .flatMap(payload ->
                        refreshTokenStore.deleteAllByIdentifier(payload.identifier())
                                .then(sessionEventPublisherPort.publishSessionEvent(
                                        new SessionEvent("session_revoked", payload.identifier(), null)))
                                .then(Mono.delay(Duration.ofMillis(100)))
                                .then(sessionEventPublisherPort.publishSessionEvent(
                                        new SessionEvent(null, payload.identifier(), null)))
                );
    }

    // logout normal (por dispositivo) debería ya notificar por device
    @Override
    public Mono<Void> logout(String refreshToken, String deviceId, String userAgent) {
        return jwtDecoderPort.decodeRefreshToken(refreshToken)
                .flatMap(payload -> refreshTokenStore.deleteByIdentifierAndDevice(payload.identifier(), deviceId, userAgent)
                        .then(sessionEventPublisherPort.publishSessionEvent(
                                new SessionEvent("session_revoked", payload.identifier(), deviceId)))
                        .then(Mono.delay(Duration.ofMillis(100)))
                        .then(sessionEventPublisherPort.publishSessionEvent(
                                new SessionEvent(null, payload.identifier(), deviceId)))
                );
    }
}
