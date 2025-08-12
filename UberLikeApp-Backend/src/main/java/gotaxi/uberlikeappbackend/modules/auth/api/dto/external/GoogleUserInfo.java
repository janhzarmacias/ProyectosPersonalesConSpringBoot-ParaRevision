package gotaxi.uberlikeappbackend.modules.auth.api.dto.external;

import lombok.Data;

@Data
public class GoogleUserInfo {
    private String id;
    private String email;
    private String name;
    private String picture; // URL de la foto
}
