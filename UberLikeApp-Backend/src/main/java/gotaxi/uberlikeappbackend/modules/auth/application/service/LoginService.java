package gotaxi.uberlikeappbackend.modules.auth.application.service;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.JwtTokensResponse;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.AuthProvider;
import gotaxi.uberlikeappbackend.modules.auth.domain.exception.AuthException;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.in.LoginUserUseCase;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.OtpRateLimiterPort;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.TokenGeneratorPort;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.UserAuthRepository;
import gotaxi.uberlikeappbackend.modules.auth.infrastructure.util.OtpKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUserUseCase {

    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGeneratorPort tokenGeneratorPort;
    private final OtpRateLimiterPort otpRateLimiter;

    @Override
    public Mono<JwtTokensResponse> loginLocal(String identifier, String rawPassword, String userAgent, String deviceId) {
        String deviceKey = OtpKeys.loginDevice(deviceId == null ? "unknown" : deviceId);
        String idKey = OtpKeys.loginId(identifier);

        return otpRateLimiter.hit(deviceKey, 5, Duration.ofMinutes(1),
                        "Demasiados intentos desde este dispositivo. Intenta más tarde.")
                .then(otpRateLimiter.hit(idKey, 10, Duration.ofMinutes(5),
                        "Demasiados intentos fallidos para este usuario. Intenta más tarde."))
                .then(userAuthRepository.findByEmailOrPhoneNumber(identifier)
                        .switchIfEmpty(Mono.error(new AuthException("Credenciales inválidas", HttpStatus.UNAUTHORIZED)))
                        .flatMap(user -> {
                            if (user.getProvider() != null && user.getProvider() != AuthProvider.LOCAL) {
                                return Mono.error(new AuthException("Por favor, inicia sesión usando Google", HttpStatus.BAD_REQUEST));
                            }
                            if (!user.isEnabled()) {
                                return Mono.error(new AuthException("Cuenta deshabilitada. Por favor, contacta con soporte.", HttpStatus.FORBIDDEN));
                            }
                            if (user.getEmail() != null && user.getEmail().equals(identifier) && !user.isEmailVerified()) {
                                return Mono.error(new AuthException("Email no verificado. Por favor, verifica tu email.", HttpStatus.FORBIDDEN));
                            } else if (user.getPhoneNumber() != null && user.getPhoneNumber().equals(identifier) && !user.isPhoneNumberVerified()) {
                                return Mono.error(new AuthException("Número de teléfono no verificado. Por favor, verifica tu teléfono.", HttpStatus.FORBIDDEN));
                            }

                            if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
                                return Mono.error(new AuthException("Credenciales inválidas", HttpStatus.UNAUTHORIZED));
                            }

                            // Login exitoso → reset de rate limits
                            return otpRateLimiter.reset(idKey)
                                    .then(otpRateLimiter.reset(deviceKey))
                                    .then(tokenGeneratorPort.generateTokens(user, userAgent, deviceId));
                        }));
    }
}
