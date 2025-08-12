package gotaxi.uberlikeappbackend.modules.auth.application.service;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.request.GoogleTokenRequest;
import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.JwtTokensResponse;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.AuthProvider;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.Role;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.UserAuth;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.in.LoginWithGoogleUseCase;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.GoogleTokenVerifier;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.TokenGeneratorPort;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginWithGoogleService implements LoginWithGoogleUseCase {

    private final UserAuthRepository userAuthRepository;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final PasswordEncoder passwordEncoder;
    private final TokenGeneratorPort tokenGeneratorPort;

    @Override
    public Mono<JwtTokensResponse> loginWithGoogle(GoogleTokenRequest request) {
        return googleTokenVerifier.verify(request.getIdToken())
                .flatMap(googleUserInfo ->
                        userAuthRepository.findByEmail(googleUserInfo.getEmail())
                                .switchIfEmpty(
                                        Mono.defer(() -> {
                                            String generatedPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
                                            return userAuthRepository.save(new UserAuth(
                                                    null,
                                                    googleUserInfo.getEmail(),
                                                    null,
                                                    generatedPasswordHash,
                                                    Role.USER,
                                                    true,
                                                    true,
                                                    false,
                                                    null,
                                                    null,
                                                    AuthProvider.GOOGLE,
                                                    googleUserInfo.getId(),
                                                    LocalDateTime.now(),
                                                    LocalDateTime.now(),
                                                    false,
                                                    request.isTermsAccepted(),
                                                    request.isPrivacyAccepted(),
                                                    request.getTermsVersion(),
                                                    request.getPrivacyVersion(),
                                                    LocalDateTime.now()
                                            ));
                                        })
                                )
                                .flatMap(existingUser -> {
                                    if (existingUser.getProvider() == AuthProvider.LOCAL) {
                                        existingUser.setProvider(AuthProvider.GOOGLE);
                                        existingUser.setProviderId(googleUserInfo.getId());
                                        return userAuthRepository.save(existingUser);
                                    }
                                    return Mono.just(existingUser);
                                })
                                .flatMap(user -> tokenGeneratorPort.generateTokens(user, request.getUserAgent(), request.getDeviceId()))

                );
    }
}
