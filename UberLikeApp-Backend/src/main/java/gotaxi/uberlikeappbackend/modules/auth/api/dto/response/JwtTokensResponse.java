package gotaxi.uberlikeappbackend.modules.auth.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtTokensResponse {
    private String accessToken;
    private String refreshToken;
    private String email;
    private String phoneNumber;
    private String role;
}
