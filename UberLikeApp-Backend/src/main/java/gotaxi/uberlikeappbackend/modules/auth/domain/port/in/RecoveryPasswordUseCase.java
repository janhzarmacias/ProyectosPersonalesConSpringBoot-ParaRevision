package gotaxi.uberlikeappbackend.modules.auth.domain.port.in;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.request.ResetPasswordRequest;
import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.JwtTokensResponse;
import reactor.core.publisher.Mono;

public interface RecoveryPasswordUseCase {

    Mono<Void> initiatePasswordRecovery(String identifier, String deviceId);
    Mono<JwtTokensResponse> resetPassword(ResetPasswordRequest request);


}