package gotaxi.uberlikeappbackend.modules.auth.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank
    private String refreshToken;

    @NotBlank
    private String deviceId;

    @NotBlank
    private String userAgent;
}
