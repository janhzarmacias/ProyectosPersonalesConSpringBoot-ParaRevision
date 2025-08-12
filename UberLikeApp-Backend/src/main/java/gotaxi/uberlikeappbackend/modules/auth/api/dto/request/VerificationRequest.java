package gotaxi.uberlikeappbackend.modules.auth.api.dto.request;

import lombok.Data;

@Data
public class VerificationRequest {
    private String email;       // Can be null if verifying by phone
    private String phoneNumber; // Can be null if verifying by email
    private String code;
}
