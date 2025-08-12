package gotaxi.uberlikeappbackend.modules.auth.api.dto.request;

import jakarta.validation.constraints.NotBlank; // Asegúrate de importar esto
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "El identificador (email o número de teléfono) es requerido.")
    private String identifier; // Este campo puede ser email o número de teléfono

    @NotBlank(message = "La contraseña es requerida.")
    private String password;

    private String deviceId;
    private String userAgent;

}