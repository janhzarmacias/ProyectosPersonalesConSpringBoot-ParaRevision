package gotaxi.uberlikeappbackend.modules.auth.api;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.request.*;
import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.ActiveSessionResponse;
import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.JwtTokensResponse;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.in.*;
import gotaxi.uberlikeappbackend.modules.auth.domain.exception.AuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; // Asegúrate de tener esta importación
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginWithGoogleUseCase loginWithGoogleUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final RecoveryPasswordUseCase recoveryPasswordUseCase;
    private final AddContactInfoUseCase addContactInfoUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetActiveSessionsUseCase getActiveSessionsUseCase;
    private final RevokeSessionUseCase revokeSessionUseCase;

    @PostMapping("/register")
    public Mono<ResponseEntity<String>> register(@Valid @RequestBody RegisterRequest req) {
        return registerUserUseCase
                .register(req.getIdentifier(), req.getPassword(),
                        req.isTermsAccepted(), req.isPrivacyAccepted(),
                        req.getTermsVersion(), req.getPrivacyVersion())
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body("User registered successfully. Please verify your account."));
    }
    @PostMapping("/verify")
    public Mono<ResponseEntity<Void>> verify(@Valid @RequestBody VerificationRequest request) {
        String identifier = request.getEmail() != null ? request.getEmail() : request.getPhoneNumber();
        if (identifier == null || identifier.trim().isEmpty()) {
            return Mono.error(new AuthException("Email or phone number must be provided for verification.", HttpStatus.BAD_REQUEST));
        }

        return registerUserUseCase.verifyCode(identifier, request.getCode())
                .thenReturn(ResponseEntity.ok().build());
    }

    @PostMapping("/login/google")
    public Mono<ResponseEntity<JwtTokensResponse>> loginWithGoogle(@RequestBody GoogleTokenRequest req) {
        return loginWithGoogleUseCase.loginWithGoogle(req)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    System.err.println("Error during Google login: " + e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<JwtTokensResponse>> login(@Valid @RequestBody LoginRequest req) {
        return loginUserUseCase.loginLocal(req.getIdentifier(), req.getPassword(), req.getUserAgent(), req.getDeviceId())
                .map(ResponseEntity::ok);
    }

    @PostMapping("/forgot-password")
    public Mono<ResponseEntity<Void>> initiatePasswordRecovery(@Valid @RequestBody InitiatePasswordRecoveryRequest request) {
        return recoveryPasswordUseCase
                .initiatePasswordRecovery(request.getIdentifier(), request.getDeviceId())
                .thenReturn(ResponseEntity.ok().<Void>build())
                .onErrorResume(AuthException.class, e -> {
                    System.err.println("AuthException durante password recovery de " + request.getIdentifier() + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.ok().<Void>build()); // oculta errores al cliente
                })
                .onErrorResume(RuntimeException.class, e -> {
                    System.err.println("Error inesperado durante password recovery de " + request.getIdentifier() + ": " + e.getMessage());
                    e.printStackTrace();
                    return Mono.just(ResponseEntity.ok().<Void>build()); // oculta errores al cliente
                });
    }

    @PostMapping("/reset-password")
    public Mono<ResponseEntity<JwtTokensResponse>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return Mono.error(new AuthException("New password and confirmation do not match.", HttpStatus.BAD_REQUEST));
        }
        return recoveryPasswordUseCase.resetPassword(
                request
        ).map(ResponseEntity::ok);
    }

    // --- ENDPOINTS PARA AÑADIR/VERIFICAR CONTACTO ---

    @PostMapping("/add-email")
    public Mono<ResponseEntity<JwtTokensResponse>> addEmail(@Valid @RequestBody AddEmailRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .flatMap(auth -> {
                    String identifier = ((UserDetails) auth.getPrincipal()).getUsername();
                    return addContactInfoUseCase.addEmailAndSendVerification(
                            identifier,
                            request.getEmail(),
                            request.getDeviceId(),
                            request.getUserAgent()
                    ).map(ResponseEntity::ok);
                });
    }


    @PostMapping("/add-phone-number")
    public Mono<ResponseEntity<JwtTokensResponse>> addPhoneNumber(@Valid @RequestBody AddPhoneNumberRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .flatMap(auth -> {
                    String identifier = ((UserDetails) auth.getPrincipal()).getUsername();
                    return addContactInfoUseCase.addPhoneNumberAndSendVerification(
                            identifier,
                            request.getPhoneNumber(),
                            request.getDeviceId(),
                            request.getUserAgent()
                    ).map(ResponseEntity::ok);
                });
    }


    @PutMapping("/change-password")
    public Mono<ResponseEntity<JwtTokensResponse>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return Mono.error(new AuthException("New password and confirmation do not match.", HttpStatus.BAD_REQUEST));
        }

        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getPrincipal())
                .cast(UserDetails.class)
                .flatMap(userDetails -> {
                    String identifier = userDetails.getUsername();
                    return changePasswordUseCase.changePassword(
                            identifier,
                            request.getCurrentPassword(),
                            request.getNewPassword(),
                            request.getDeviceId(),
                            request.getUserAgent()
                    );
                })
                .map(ResponseEntity::ok);
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<JwtTokensResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return refreshTokenUseCase.refresh(
                request.getRefreshToken(),
                request.getUserAgent(),
                request.getDeviceId()
        ).map(ResponseEntity::ok);
    }

    //logouts

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(@RequestHeader("Authorization") String refreshToken,
                                             @RequestBody LogoutRequest request) {
        return logoutUseCase.logout(extractToken(refreshToken), request.deviceId(), request.userAgent())
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping("/logout/all")
    public Mono<ResponseEntity<Void>> logoutAll(@RequestHeader("Authorization") String accessToken) {
        return logoutUseCase.logoutAll(extractToken(accessToken))
                .thenReturn(ResponseEntity.noContent().build());
    }

    private String extractToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header format");
        }
        return header.substring(7).trim();
    }


    @GetMapping("/sessions")
    public Mono<ResponseEntity<List<ActiveSessionResponse>>> getSessions(ServerHttpRequest request) {
        String jwt = extractToken(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        return getActiveSessionsUseCase.getSessionsFromToken(jwt)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/close/session")
    public Mono<ResponseEntity<Void>> logoutFromSpecificSession(
            ServerHttpRequest request,
            @RequestBody SessionRevokeRequest revokeRequest
    ) {
        String jwt = extractToken(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        return revokeSessionUseCase
                .revokeSessionFromJwt(jwt, revokeRequest.deviceId(), revokeRequest.userAgent())
                .thenReturn(ResponseEntity.noContent().build());
    }








}