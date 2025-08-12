package gotaxi.uberlikeappbackend.modules.auth.application.service;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.JwtTokensResponse;
import gotaxi.uberlikeappbackend.modules.auth.domain.exception.AuthException;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.SessionEvent;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.in.ChangePasswordUseCase;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChangePasswordService implements ChangePasswordUseCase {

    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGeneratorPort tokenGeneratorPort;

    // 👇 añade estos dos
    private final RefreshTokenStore refreshTokenStore;
    private final SessionEventPublisherPort sessionEventPublisherPort;


    @Override
    public Mono<JwtTokensResponse> changePassword(
            String identifier,
            String currentPassword,
            String newPassword,
            String deviceId,
            String userAgent
    ) {
        return userAuthRepository.findByEmailOrPhoneNumber(identifier)
                .switchIfEmpty(Mono.error(new AuthException("User not found.", HttpStatus.UNAUTHORIZED)))
                .flatMap(user -> {
                    if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
                        return Mono.error(new AuthException("New password must be different from the current one.", HttpStatus.BAD_REQUEST));
                    }
                    if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                        return Mono.error(new AuthException("Current password is incorrect.", HttpStatus.UNAUTHORIZED));
                    }

                    user.setPasswordHash(passwordEncoder.encode(newPassword));
                    user.setUpdatedAt(LocalDateTime.now());

                    return userAuthRepository.save(user)
                            .flatMap(saved ->
                                    refreshTokenStore.deleteAllByIdentifier(identifier)
                                            .then(sessionEventPublisherPort.publishSessionEvent(
                                                    new SessionEvent("session_revoked", identifier, null)))
                                            .then(Mono.delay(Duration.ofMillis(100)))
                                            .then(sessionEventPublisherPort.publishSessionEvent(
                                                    new SessionEvent(null, identifier, null)))
                                            .then(tokenGeneratorPort.generateTokens(saved, userAgent, deviceId))
                            );
                });
    }
}
