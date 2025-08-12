package gotaxi.uberlikeappbackend.modules.auth.application.service;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.JwtTokensResponse;
import gotaxi.uberlikeappbackend.modules.auth.domain.exception.AuthException;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.AuthProvider;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.UserAuth;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.in.AddContactInfoUseCase;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.TokenGeneratorPort;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.UserAuthRepository;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.UserNotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

@Service
@RequiredArgsConstructor
public class AddContactInfoService implements AddContactInfoUseCase {

    private final UserAuthRepository userAuthRepository;
    private final UserNotificationPort userNotificationPort;
    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    private final TokenGeneratorPort tokenGeneratorPort;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    private boolean isExternalProvider(UserAuth user) {
        return !AuthProvider.LOCAL.equals(user.getProvider());
    }

    @Override
    public Mono<JwtTokensResponse> addEmailAndSendVerification(String authenticatedUserIdentifier, String newEmail, String deviceId, String userAgent) {
        if (!isValidEmail(newEmail)) {
            return Mono.error(new AuthException("Invalid email format.", HttpStatus.BAD_REQUEST));
        }

        return userAuthRepository.findByEmailOrPhoneNumber(authenticatedUserIdentifier)
                .switchIfEmpty(Mono.error(new AuthException("Authenticated user not found.", HttpStatus.UNAUTHORIZED)))
                .flatMap(user -> {

                    //no permite cambiar un email de alguien que hizo OAuth con google
                    if (isExternalProvider(user)) {
                        return Mono.error(new AuthException("This email is from your Google account. You cannot change it.", HttpStatus.CONFLICT));
                    }

                    // Si ya tiene este email verificado, error
                    if (user.isEmailVerified() && user.getEmail() != null && user.getEmail().equals(newEmail)) {
                        return Mono.error(new AuthException("This email is already your current verified email.", HttpStatus.CONFLICT));
                    }

                    // Si ya está en proceso de verificación para este email
                    if (user.getEmail() != null && user.getEmail().equals(newEmail) && user.getVerificationCode() != null) {
                        LocalDateTime expiresAt = user.getVerificationCodeExpiresAt();
                        if (expiresAt != null && LocalDateTime.now().isBefore(expiresAt)) {
                            return Mono.error(new AuthException(
                                    "A pending verification is active for this email. Please complete it or wait until it expires.",
                                    HttpStatus.CONFLICT
                            ));
                        }
                    }

                    // Solo permite cambiar el email si tienes phone verificado
                    if (user.isEmailVerified() && (user.getPhoneNumber() == null || !user.isPhoneNumberVerified())) {
                        return Mono.error(new AuthException(
                                "To change your email, you must first have a verified phone number as a backup.",
                                HttpStatus.BAD_REQUEST));
                    }

                    // Verificar duplicados, liberar si es necesario
                    return releaseEmailIfUnverified(newEmail)
                            .then(userAuthRepository.existsByEmail(newEmail))
                            .flatMap(emailExists -> {
                                if (emailExists) {
                                    return Mono.error(new AuthException("Email is already in use.", HttpStatus.CONFLICT));
                                }

                                user.setEmail(newEmail);
                                user.setEmailVerified(false);
                                String verificationCode = generateVerificationCode();
                                user.setVerificationCode(verificationCode);
                                user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(2));

                                return userAuthRepository.save(user)
                                        .flatMap(savedUser -> {
                                            userNotificationPort.sendVerificationEmail(newEmail, verificationCode);
                                            return tokenGeneratorPort.generateTokens(savedUser, userAgent, deviceId);
                                        });
                            });
                })
                .onErrorResume(DuplicateKeyException.class, e ->
                        Mono.error(new AuthException("Email is already in use.", HttpStatus.CONFLICT))
                );
    }



    @Override
    public Mono<JwtTokensResponse> addPhoneNumberAndSendVerification(String authenticatedUserIdentifier, String newPhoneNumber, String deviceId, String userAgent) {
        if (!isValidPhoneNumber(newPhoneNumber)) {
            return Mono.error(new AuthException("Invalid phone number format.", HttpStatus.BAD_REQUEST));
        }

        return userAuthRepository.findByEmailOrPhoneNumber(authenticatedUserIdentifier)
                .switchIfEmpty(Mono.error(new AuthException("Authenticated user not found.", HttpStatus.UNAUTHORIZED)))
                .flatMap(user -> {
                    // No permitir cambiar por el mismo número actual
                    if (user.isPhoneNumberVerified() && user.getPhoneNumber() != null && user.getPhoneNumber().equals(newPhoneNumber)) {
                        return Mono.error(new AuthException("This phone number is already your current verified phone.", HttpStatus.CONFLICT));
                    }

                    // Ya está intentando verificar el mismo número
                    if (user.getPhoneNumber() != null && user.getPhoneNumber().equals(newPhoneNumber) && user.getVerificationCode() != null) {
                        LocalDateTime expiresAt = user.getVerificationCodeExpiresAt();
                        if (expiresAt != null && LocalDateTime.now().isBefore(expiresAt)) {
                            return Mono.error(new AuthException(
                                    "A pending verification is active for this phone number. Please complete it or wait until it expires.",
                                    HttpStatus.CONFLICT
                            ));
                        }
                    }

                    // Solo permitir si tiene email verificado como respaldo
                    if (user.isPhoneNumberVerified() && (user.getEmail() == null || !user.isEmailVerified())) {
                        return Mono.error(new AuthException(
                                "To change your number, you must first have a verified email as a backup.",
                                HttpStatus.BAD_REQUEST));
                    }

                    // Lógica de liberar y verificar duplicados
                    return releasePhoneIfUnverified(newPhoneNumber)
                            .then(userAuthRepository.existsByPhoneNumber(newPhoneNumber))
                            .flatMap(phoneExists -> {
                                if (phoneExists) {
                                    return Mono.error(new AuthException("Phone number is already in use.", HttpStatus.CONFLICT));
                                }

                                user.setPhoneNumber(newPhoneNumber);
                                user.setPhoneNumberVerified(false);
                                String verificationCode = generateVerificationCode();
                                user.setVerificationCode(verificationCode);
                                user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(2));
                                user.setUpdatedAt(LocalDateTime.now());

                                return userAuthRepository.save(user)
                                        .flatMap(savedUser -> {
                                            userNotificationPort.sendVerificationWhatsApp(newPhoneNumber, verificationCode);
                                            return tokenGeneratorPort.generateTokens(savedUser, userAgent, deviceId);
                                        });
                            });
                })
                .onErrorResume(DuplicateKeyException.class, e ->
                        Mono.error(new AuthException("Phone number is already in use.", HttpStatus.CONFLICT))
                );
    }




    private String generateVerificationCode() {
        return String.valueOf((int) (Math.random() * 9000) + 1000);
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
            System.err.println("DEBUG: Failed to parse phone number for adding contact: " + phoneNumber + ": " + e.getMessage());
            return false;
        }
    }

    private Mono<Void> releasePhoneIfUnverified(String phoneNumber) {
        return userAuthRepository.findByPhoneNumber(phoneNumber)
                .filter(existingUser -> !existingUser.isDeleted())
                .flatMap(existingUser -> {
                    if (!existingUser.isPhoneNumberVerified()) {
                        // Verifica si el código está activo o expirado
                        LocalDateTime expiresAt = existingUser.getVerificationCodeExpiresAt();
                        boolean codeIsActive = expiresAt != null && LocalDateTime.now().isBefore(expiresAt);

                        if (codeIsActive) {
                            // No hacer nada si el código está activo
                            return Mono.empty();
                        }

                        // Si ya expiró, libera
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
                    // Si ya está verificado, no liberar
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
                            // No hacer nada si el código está activo
                            return Mono.empty();
                        }
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
