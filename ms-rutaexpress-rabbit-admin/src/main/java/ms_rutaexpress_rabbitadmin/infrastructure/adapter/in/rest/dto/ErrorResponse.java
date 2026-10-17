package ms_rutaexpress_rabbitadmin.infrastructure.adapter.in.rest.dto;

import java.time.LocalDateTime;
import java.util.List;

// Formato único de error para todos los endpoints
public record ErrorResponse(
        int status,
        String error,
        List<String> detalles,
        String path,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(int status, String error, List<String> detalles, String path) {
        return new ErrorResponse(status, error, detalles, path, LocalDateTime.now());
    }
}
