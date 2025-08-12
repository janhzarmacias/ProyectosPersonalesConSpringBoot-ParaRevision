package gotaxi.uberlikeappbackend.modules.auth.api.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class InitiatePasswordRecoveryRequest {
        @NotBlank private String identifier;
        @NotBlank private String deviceId;
}