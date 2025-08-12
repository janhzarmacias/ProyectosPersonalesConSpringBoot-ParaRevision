package gotaxi.uberlikeappbackend.modules.auth.domain.port.in;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.JwtTokensResponse;
import reactor.core.publisher.Mono;

public interface AddContactInfoUseCase {
    Mono<JwtTokensResponse> addEmailAndSendVerification(String identifier, String email, String deviceId, String userAgent);
    Mono<JwtTokensResponse> addPhoneNumberAndSendVerification(String identifier, String phoneNumber, String deviceId, String userAgent);
}