package gotaxi.uberlikeappbackend.modules.auth.application.service;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import gotaxi.uberlikeappbackend.modules.auth.api.dto.request.ResetPasswordRequest;
import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.JwtTokensResponse;
import gotaxi.uberlikeappbackend.modules.auth.domain.exception.AuthException;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.PasswordResetToken;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.SessionEvent;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.in.RecoveryPasswordUseCase;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.*;
import gotaxi.uberlikeappbackend.modules.auth.infrastructure.util.OtpKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RecoveryPasswordService implements RecoveryPasswordUseCase {

    private final UserAuthRepository userAuthRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final UserNotificationPort userNotificationPort;
    private final PasswordEncoder passwordEncoder;
    private final TokenGeneratorPort tokenGeneratorPort;
    private final OtpRateLimiterPort otpRateLimiter;

    private final RefreshTokenStore refreshTokenStore;
    private final SessionEventPublisherPort sessionEventPublisherPort;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final SecureRandom OTP_GENERATOR = new SecureRandom();
    private static final int OTP_EXPIRATION_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    @Override
    public Mono<Void> initiatePasswordRecovery(String identifier, String deviceId) {
        return userAuthRepository.findByEmailOrPhoneNumber(identifier)
                .switchIfEmpty(Mono.error(new AuthException("User not found.", HttpStatus.NOT_FOUND)))
                .flatMap(user -> {
                    boolean canSend = false;

                    if (isValidEmail(identifier)) {
                        canSend = user.isEmailVerified() && identifier.equals(user.getEmail());
                    } else if (isValidPhoneNumber(identifier)) {
                        canSend = user.isPhoneNumberVerified() && identifier.equals(user.getPhoneNumber());
                    }

                    if (!canSend) {
                        return Mono.error(new AuthException("Cannot send recovery code: contact not verified.", HttpStatus.FORBIDDEN));
                    }

                    // Rate limit de envíos por identifier
                    return otpRateLimiter.hit(
                                    OtpKeys.sendId(identifier),
                                    3,
                                    Duration.ofMinutes(10),
                                    "Too many recovery code requests. Try again later."
                            )
                            // Resetear bloqueos de verificación anteriores
                            .then(otpRateLimiter.reset(OtpKeys.vId(identifier)))
                            .then(otpRateLimiter.reset(OtpKeys.vPreDev(deviceId == null ? "unknown" : deviceId)))
                            .then(tokenRepository.deleteByUserId(user.getId()))
                            .then(Mono.defer(() -> {
                                String token = generateOtp6Digits();
                                LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);
                                PasswordResetToken newToken = new PasswordResetToken(token, user.getId(), expiresAt);

                                return tokenRepository.save(newToken)
                                        .doOnSuccess(t -> userNotificationPort.sendPasswordResetCode(identifier, token))
                                        .then();
                            }));
                });
    }

    @Override
    public Mono<JwtTokensResponse> resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return Mono.error(new AuthException("Passwords do not match.", HttpStatus.BAD_REQUEST));
        }

        final String identifier = request.getIdentifier();
        final String deviceId = request.getDeviceId() == null ? "unknown" : request.getDeviceId();
        final String deviceRateLimitKey = OtpKeys.vPreDev(deviceId);
        final String identifierRateLimitKey = OtpKeys.vId(identifier);

        return otpRateLimiter.hit(
                deviceRateLimitKey,
                5,
                Duration.ofMinutes(1),
                "Too many attempts from this device. Try again later."
        ).then(
                userAuthRepository.findByEmailOrPhoneNumber(identifier)
                        .switchIfEmpty(Mono.error(new AuthException("User not found.", HttpStatus.NOT_FOUND)))
                        .flatMap(user ->
                                tokenRepository.findLatestByUserId(user.getId())
                                        .switchIfEmpty(Mono.error(new AuthException("No active OTP. Request a new one.", HttpStatus.BAD_REQUEST)))
                                        .flatMap(token -> {

                                            // Validaciones de OTP
                                            if (token.isExpired()) {
                                                return Mono.error(new AuthException("OTP expired. Request a new one.", HttpStatus.BAD_REQUEST));
                                            }

                                            if (token.isUsed()) {
                                                return Mono.error(new AuthException("OTP already used. Request a new one.", HttpStatus.BAD_REQUEST));
                                            }

                                            if (!token.getToken().equals(request.getToken())) {
                                                token.setAttempts(token.getAttempts() + 1);

                                                return tokenRepository.save(token)
                                                        .then(otpRateLimiter.hit(
                                                                identifierRateLimitKey,
                                                                MAX_ATTEMPTS,
                                                                Duration.ofMinutes(10),
                                                                "Too many incorrect codes. OTP blocked. Request a new one."
                                                        ))
                                                        .then(Mono.error(new AuthException("Incorrect OTP.", HttpStatus.BAD_REQUEST)));
                                            }

                                            // OTP correcto → actualizar password, marcar OTP como usado, revocar sesiones y generar nuevos tokens
                                            token.setUsed(true);
                                            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                                            user.setUpdatedAt(LocalDateTime.now());

                                            return userAuthRepository.save(user)
                                                    .then(tokenRepository.save(token))
                                                    .then(refreshTokenStore.deleteAllByIdentifier(identifier))
                                                    .then(sessionEventPublisherPort.publishSessionEvent(
                                                            new SessionEvent("session_revoked", identifier, null)))
                                                    .then(Mono.delay(Duration.ofMillis(100)))
                                                    .then(sessionEventPublisherPort.publishSessionEvent(
                                                            new SessionEvent(null, identifier, null)))
                                                    .then(tokenGeneratorPort.generateTokens(user, request.getUserAgent(), deviceId))
                                                    .flatMap(tokens -> otpRateLimiter.reset(identifierRateLimitKey).thenReturn(tokens));
                                        })
                        )
        );
    }


    private String generateOtp6Digits() {
        return String.format("%06d", OTP_GENERATOR.nextInt(1_000_000));
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) return false;
        try {
            Phonenumber.PhoneNumber n = phoneNumberUtil.parse(phoneNumber, "ZZ");
            return phoneNumberUtil.isValidNumber(n);
        } catch (Exception e) {
            return false;
        }
    }
}
