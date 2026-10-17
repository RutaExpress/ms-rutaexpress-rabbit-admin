package ms_rutaexpress_rabbitadmin.domain.exception;

// Regla de negocio incumplida (combinación de datos inválida) -> 400
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
