package gotaxi.uberlikeappbackend.modules.auth.domain.exception;

import org.springframework.http.HttpStatus; // Importar HttpStatus

public class AuthException extends RuntimeException {

    private final HttpStatus status; // Campo para el código de estado HTTP

    public AuthException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    // opcional un constructor sin status (usaría un default, ej. INTERNAL_SERVER_ERROR)
    public AuthException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR; // Default si no se especifica
    }
}