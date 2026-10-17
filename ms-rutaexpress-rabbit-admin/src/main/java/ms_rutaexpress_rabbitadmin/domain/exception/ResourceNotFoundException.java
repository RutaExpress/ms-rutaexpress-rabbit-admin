package ms_rutaexpress_rabbitadmin.domain.exception;

// La cola o exchange no existe -> 404
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
