package gotaxi.uberlikeappbackend.modules.auth.infrastructure.adapter;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.JwtTokensResponse;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.RefreshTokenMetadata;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.UserAuth;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.TokenGeneratorPort;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.RefreshTokenRegisterPort;
import gotaxi.uberlikeappbackend.modules.auth.infrastructure.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TokenGeneratorAdapter implements TokenGeneratorPort {

    private final JwtService jwtService;
    private final RefreshTokenRegisterPort refreshTokenRegisterPort;
    private static final Logger log = LoggerFactory.getLogger(TokenGeneratorAdapter.class);



    @Override
    public Mono<JwtTokensResponse> generateTokens(UserAuth user, String userAgent, String deviceId) {
        String newIdentifier = user.getEmail() != null && user.isEmailVerified()
                ? user.getEmail()
                : user.getPhoneNumber();

        if (newIdentifier == null || newIdentifier.isBlank()) {
            throw new IllegalStateException("No valid identifier found for refresh token metadata.");
        }

        String accessToken = jwtService.generateToken(newIdentifier, user.getRole().name());

        RefreshTokenMetadata metadata = new RefreshTokenMetadata(
                newIdentifier,
                deviceId,
                userAgent,
                LocalDateTime.now()
        );

        // Intentamos limpiar el identifier anterior si existe
        Mono<Void> cleanup = Mono.empty();

        if (user.getEmail() != null && user.isEmailVerified() && user.getPhoneNumber() != null && user.isPhoneNumberVerified()) {
            String oldIdentifier = user.getPhoneNumber();
            String oldIndexKey = "refresh:index:" + oldIdentifier + ":" + userAgent + ":" + deviceId;
            String oldRefreshKeyPrefix = "refresh:";

            cleanup = refreshTokenRegisterPort.getRefreshTokenByIndex(oldIdentifier, deviceId, userAgent)
                    .flatMap(oldToken -> {
                        if (oldToken != null) {
                            log.info("Eliminando refresh token huérfano con identificador anterior: {}", oldIdentifier);
                            return refreshTokenRegisterPort.deleteKeys(oldIndexKey, oldRefreshKeyPrefix + oldToken);
                        } else {
                            log.info("No se encontró token huérfano para limpiar con identificador anterior: {}", oldIdentifier);
                            return Mono.empty();
                        }
                    });
        }

        return cleanup.then(refreshTokenRegisterPort.registerRefreshToken(metadata))
                .map(refreshToken -> new JwtTokensResponse(
                        accessToken,
                        refreshToken,
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getRole().name()
                ));
    }


}
