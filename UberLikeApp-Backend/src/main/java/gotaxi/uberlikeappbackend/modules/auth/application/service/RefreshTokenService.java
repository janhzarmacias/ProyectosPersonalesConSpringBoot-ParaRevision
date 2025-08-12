package gotaxi.uberlikeappbackend.modules.auth.application.service;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.JwtTokensResponse;
import gotaxi.uberlikeappbackend.modules.auth.domain.exception.AuthException;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.UserAuth;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.in.RefreshTokenUseCase;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.RefreshTokenStore;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.TokenGeneratorPort;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private final RefreshTokenStore refreshTokenStore;
    private final UserAuthRepository userAuthRepository;
    private final TokenGeneratorPort tokenGeneratorPort;

    @Override
    public Mono<JwtTokensResponse> refresh(String refreshToken, String userAgent, String deviceId) {
        return refreshTokenStore.getMetadataFromRefreshToken(refreshToken)
                .switchIfEmpty(Mono.error(new AuthException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED)))
                .flatMap(metadata -> {
                    // Validar que coincida device y agente
                    if (!metadata.getDeviceId().equals(deviceId) || !metadata.getUserAgent().equals(userAgent)) {
                        return Mono.error(new AuthException("Refresh token metadata does not match this device", HttpStatus.UNAUTHORIZED));
                    }

                    // Buscar usuario actual con el identifier del token viejo
                    return userAuthRepository.findByEmailOrPhoneNumber(metadata.getIdentifier())
                            .switchIfEmpty(Mono.error(new AuthException("User not found", HttpStatus.UNAUTHORIZED)))
                            .flatMap(user -> {
                                // Determinar el identifier actual del usuario
                                String currentIdentifier = user.getEmail() != null ? user.getEmail() : user.getPhoneNumber();

                                // Validar que el identifier del refresh token coincida con el actual
                                if (!metadata.getIdentifier().equals(currentIdentifier)) {
                                    return Mono.error(new AuthException("Refresh token is outdated. Please login again.", HttpStatus.UNAUTHORIZED));
                                }

                                // Eliminar el viejo y generar nuevo
                                return refreshTokenStore.deleteRefreshToken(refreshToken)
                                        .then(tokenGeneratorPort.generateTokens(user, userAgent, deviceId));
                            });
                });
    }
}

