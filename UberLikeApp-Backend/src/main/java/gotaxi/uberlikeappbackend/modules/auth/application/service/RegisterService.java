package gotaxi.uberlikeappbackend.modules.auth.application.service;

import gotaxi.uberlikeappbackend.modules.auth.domain.port.in.RegisterUserUseCase;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.OtpRateLimiterPort;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.UserAuthRepository;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.Role;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.UserAuth;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.AuthProvider;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.UserNotificationPort;
import gotaxi.uberlikeappbackend.modules.auth.domain.exception.AuthException;
import gotaxi.uberlikeappbackend.modules.auth.infrastructure.util.OtpKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

@Service
@RequiredArgsConstructor
public class RegisterService implements RegisterUserUseCase {
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserNotificationPort userNotificationPort;
    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    private final OtpRateLimiterPort otpRateLimiter;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    private static final int VERIFICATION_CODE_EXPIRATION_MINUTES = 2;

    @Override
    public Mono<Void> register(String identifier, String password,boolean termsAccepted, boolean privacyAccepted,
                               String termsVersion, String privacyVersion) {
        boolean isEmail = isValidEmail(identifier);
        boolean isPhone = isValidPhoneNumber(identifier);

        if (!isEmail && !isPhone) {
            return Mono.error(new AuthException("Identifier must be a valid email or phone number.", HttpStatus.BAD_REQUEST));
        }

        // Reutiliza los métodos de liberar y verificar según sea email o phone
        Mono<Void> liberar = isEmail ? releaseEmailIfUnverified(identifier) : releasePhoneIfUnverified(identifier);

        return liberar.then(
                userAuthRepository.findByEmailOrPhoneNumber(identifier)
                        .flatMap(existingUser -> {
                            boolean verified = isEmail ? existingUser.isEmailVerified() : existingUser.isPhoneNumberVerified();
                            if (verified) {
                                String campo = isEmail ? "Email" : "Phone Number";
                                return Mono.error(new AuthException(campo + " already registered.", HttpStatus.CONFLICT));
                            }

                            LocalDateTime expiresAt = existingUser.getVerificationCodeExpiresAt();
                            if (expiresAt != null && LocalDateTime.now().isBefore(expiresAt)) {
                                return Mono.error(new AuthException("A pending verification is active for this identifier. Please complete it or wait until it expires.", HttpStatus.CONFLICT));
                            }

                            String verificationCode = generateVerificationCode();
                            existingUser.setVerificationCode(verificationCode);
                            existingUser.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRATION_MINUTES));

                            //persistir consentimientos
                            existingUser.setTermsAccepted(termsAccepted);
                            existingUser.setPrivacyAccepted(privacyAccepted);
                            existingUser.setTermsVersion(termsVersion);
                            existingUser.setPrivacyVersion(privacyVersion);
                            if (existingUser.getConsentAt() == null) {
                                existingUser.setConsentAt(LocalDateTime.now());
                            }
                            existingUser.setUpdatedAt(LocalDateTime.now());

                            if (isEmail) userNotificationPort.sendVerificationEmail(identifier, verificationCode);
                            else userNotificationPort.sendVerificationWhatsApp(identifier, verificationCode);

                            return userAuthRepository.save(existingUser).then();
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            // No existe, crea usuario
                            String encodedPassword = passwordEncoder.encode(password);
                            String verificationCode = generateVerificationCode();
                            LocalDateTime now = LocalDateTime.now();
                            LocalDateTime expiresAt = now.plusMinutes(VERIFICATION_CODE_EXPIRATION_MINUTES);

                            UserAuth newUser = new UserAuth(
                                    null,
                                    isEmail ? identifier : null,
                                    isPhone ? identifier : null,
                                    encodedPassword,
                                    Role.USER,
                                    true,              // enabled
                                    false,             // isEmailVerified
                                    false,             // isPhoneNumberVerified
                                    verificationCode,
                                    expiresAt,
                                    AuthProvider.LOCAL,
                                    null,
                                    now,
                                    now,
                                    false,
                                    termsAccepted,
                                    privacyAccepted,
                                    termsVersion,
                                    privacyVersion,
                                    now // consentAt
                            );
                            return userAuthRepository.save(newUser)
                                    .flatMap(savedUser -> {
                                        if (isEmail) userNotificationPort.sendVerificationEmail(identifier, verificationCode);
                                        else userNotificationPort.sendVerificationWhatsApp(identifier, verificationCode);
                                        return Mono.<Void>empty();
                                    });
                        }))
        ).onErrorResume(DuplicateKeyException.class, e -> {
                    System.err.println("Duplicate registration race condition: " + identifier);
                    return Mono.<Void>empty();
        }).onErrorResume(ex -> {
            if (ex instanceof AuthException) {
                return Mono.error(ex);
            }
            System.err.println("Other registration error: " + ex.getMessage());
            return Mono.error(new AuthException("Failed to register user.", HttpStatus.INTERNAL_SERVER_ERROR));
        });
    }

    @Override
    public Mono<Void> verifyCode(String identifier, String code) {
        String rateLimitKey = OtpKeys.vId(identifier); // puedes usar también deviceId si lo tienes

        return otpRateLimiter.hit(rateLimitKey, 5, Duration.ofMinutes(2),
                        "Too many incorrect verification attempts. Try again later.")
                .then(userAuthRepository.findByEmailOrPhoneNumber(identifier)
                        .switchIfEmpty(Mono.error(new AuthException("User not found for verification.", HttpStatus.NOT_FOUND)))
                        .flatMap(user -> {
                            if (user.getVerificationCode() == null || !user.getVerificationCode().equals(code)) {
                                return Mono.error(new AuthException("Invalid verification code.", HttpStatus.BAD_REQUEST));
                            }
                            if (user.getVerificationCodeExpiresAt() == null || LocalDateTime.now().isAfter(user.getVerificationCodeExpiresAt())) {
                                return Mono.error(new AuthException("Verification code has expired.", HttpStatus.BAD_REQUEST));
                            }

                            boolean isEmailIdentifier = isValidEmail(identifier);
                            boolean isPhoneIdentifier = isValidPhoneNumber(identifier);

                            if (isEmailIdentifier && identifier.equals(user.getEmail())) {
                                if (user.isEmailVerified()) {
                                    return Mono.error(new AuthException("Email is already verified.", HttpStatus.CONFLICT));
                                }
                                user.setEmailVerified(true);
                            } else if (isPhoneIdentifier && identifier.equals(user.getPhoneNumber())) {
                                if (user.isPhoneNumberVerified()) {
                                    return Mono.error(new AuthException("Phone number is already verified.", HttpStatus.CONFLICT));
                                }
                                user.setPhoneNumberVerified(true);
                            } else {
                                return Mono.error(new AuthException("Discrepancy in verification identifier.", HttpStatus.INTERNAL_SERVER_ERROR));
                            }

                            user.setVerificationCode(null);
                            user.setVerificationCodeExpiresAt(null);

                            return userAuthRepository.save(user)
                                    .then(otpRateLimiter.reset(rateLimitKey)) // reset al acertar
                                    .then();
                        })
                );
    }

    private String generateVerificationCode() {
        return String.valueOf((int)(Math.random() * 9000) + 1000);
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        try {
            PhoneNumber number = phoneNumberUtil.parse(phoneNumber, "ZZ");
            return phoneNumberUtil.isValidNumber(number);
        } catch (com.google.i18n.phonenumbers.NumberParseException e) {
            System.err.println("DEBUG: Failed to parse phone number for registration: " + phoneNumber + ": " + e.getMessage());
            return false;
        }
    }


    private Mono<Void> releasePhoneIfUnverified(String phoneNumber) {
        return userAuthRepository.findByPhoneNumber(phoneNumber)
                .filter(existingUser -> !existingUser.isDeleted())
                .flatMap(existingUser -> {
                    if (!existingUser.isPhoneNumberVerified()) {
                        LocalDateTime expiresAt = existingUser.getVerificationCodeExpiresAt();
                        boolean codeIsActive = expiresAt != null && LocalDateTime.now().isBefore(expiresAt);
                        if (codeIsActive) {
                            // Código activo, no liberar todavía
                            return Mono.empty();
                        }
                        // Código expirado: liberar campo o soft delete
                        if (existingUser.getEmail() != null && !existingUser.getEmail().isBlank()) {
                            existingUser.setPhoneNumber(null);
                            existingUser.setPhoneNumberVerified(false);
                            existingUser.setUpdatedAt(LocalDateTime.now());
                            return userAuthRepository.save(existingUser).then();
                        } else {
                            existingUser.setDeleted(true);
                            existingUser.setUpdatedAt(LocalDateTime.now());
                            return userAuthRepository.save(existingUser).then();
                        }
                    }
                    return Mono.empty();
                })
                .switchIfEmpty(Mono.empty())
                .onErrorResume(e -> {
                    System.err.println("Error in releasePhoneIfUnverified: " + e.getMessage());
                    return Mono.empty();
                });
    }

    private Mono<Void> releaseEmailIfUnverified(String email) {
        return userAuthRepository.findByEmail(email)
                .filter(existingUser -> !existingUser.isDeleted())
                .flatMap(existingUser -> {
                    if (!existingUser.isEmailVerified()) {
                        LocalDateTime expiresAt = existingUser.getVerificationCodeExpiresAt();
                        boolean codeIsActive = expiresAt != null && LocalDateTime.now().isBefore(expiresAt);
                        if (codeIsActive) {
                            // Código activo, no liberar todavía
                            return Mono.empty();
                        }
                        // Código expirado: liberar campo o soft delete
                        if (existingUser.getPhoneNumber() != null && !existingUser.getPhoneNumber().isBlank()) {
                            existingUser.setEmail(null);
                            existingUser.setEmailVerified(false);
                            existingUser.setUpdatedAt(LocalDateTime.now());
                            return userAuthRepository.save(existingUser).then();
                        } else {
                            existingUser.setDeleted(true);
                            existingUser.setUpdatedAt(LocalDateTime.now());
                            return userAuthRepository.save(existingUser).then();
                        }
                    }
                    return Mono.empty();
                })
                .switchIfEmpty(Mono.empty())
                .onErrorResume(e -> {
                    System.err.println("Error in releaseEmailIfUnverified: " + e.getMessage());
                    return Mono.empty();
                });
    }

}

