package gotaxi.uberlikeappbackend.modules.common.exception;

import gotaxi.uberlikeappbackend.modules.auth.domain.exception.AuthException; // ¡Importa tu nueva excepción!
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Este méttodo capturará específicamente AuthException
    @ExceptionHandler(AuthException.class)
    public Mono<ResponseEntity<String>> handleAuthException(AuthException ex) {
        System.err.println("AuthException capturada: " + ex.getMessage());
        // No es necesario el ex.printStackTrace() en producción, un logger es mejor.
        // ex.printStackTrace(); // Para depuración en desarrollo

        // El status y el mensaje vienen directamente de la excepción
        return Mono.just(ResponseEntity.status(ex.getStatus()).body(ex.getMessage()));
    }

    // **IMPORTANTE:** Mantén un manejador genérico para RuntimeException
    // por si alguna otra parte de tu código lanza una RuntimeException que no sea AuthException.
    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<String>> handleGenericRuntimeException(RuntimeException ex) {
        System.err.println("Generic RuntimeException capturada por GlobalExceptionHandler: " + ex.getMessage());
        ex.printStackTrace(); // Log completo para errores inesperados

        // Para errores inesperados, siempre devuelve un 500 y un mensaje genérico al cliente
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected server error occurred. Please try again later."));
    }

    // ... otros @ExceptionHandler si tienes para otras excepciones de Spring, etc.
}
