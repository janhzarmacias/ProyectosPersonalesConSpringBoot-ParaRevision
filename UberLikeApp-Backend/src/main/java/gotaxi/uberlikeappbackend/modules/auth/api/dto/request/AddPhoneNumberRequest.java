package gotaxi.uberlikeappbackend.modules.auth.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddPhoneNumberRequest {
    @NotBlank(message = "New phone number is required")
    private String phoneNumber;

    private String userAgent; // opcional
    private String deviceId;  // opcional
}