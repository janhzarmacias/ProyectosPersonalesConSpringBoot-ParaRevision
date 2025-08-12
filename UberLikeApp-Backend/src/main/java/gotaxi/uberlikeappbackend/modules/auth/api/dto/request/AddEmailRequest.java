package gotaxi.uberlikeappbackend.modules.auth.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddEmailRequest {
    @NotBlank(message = "New email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String userAgent; // opcional
    private String deviceId;  // opcional

}