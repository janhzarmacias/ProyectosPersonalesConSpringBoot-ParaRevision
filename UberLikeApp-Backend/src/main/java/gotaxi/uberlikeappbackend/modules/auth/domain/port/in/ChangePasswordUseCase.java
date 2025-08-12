package gotaxi.uberlikeappbackend.modules.auth.domain.port.in;

import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.JwtTokensResponse;
import reactor.core.publisher.Mono;

public interface ChangePasswordUseCase {
    Mono<JwtTokensResponse> changePassword(
            String identifier,
            String currentPassword,
            String newPassword,
            String deviceId,
            String userAgent
    );
}
