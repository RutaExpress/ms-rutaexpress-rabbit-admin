package ms_rutaexpress_rabbitadmin.domain.exception;

// La cola o exchange ya existe -> 409
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
