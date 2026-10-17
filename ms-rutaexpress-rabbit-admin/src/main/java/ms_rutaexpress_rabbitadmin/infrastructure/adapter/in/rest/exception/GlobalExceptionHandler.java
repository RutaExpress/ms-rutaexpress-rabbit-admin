package ms_rutaexpress_rabbitadmin.infrastructure.adapter.in.rest.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import ms_rutaexpress_rabbitadmin.domain.exception.InvalidRequestException;
import ms_rutaexpress_rabbitadmin.domain.exception.ResourceAlreadyExistsException;
import ms_rutaexpress_rabbitadmin.domain.exception.ResourceNotFoundException;
import ms_rutaexpress_rabbitadmin.infrastructure.adapter.in.rest.dto.ErrorResponse;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.List;

// Traduce cada excepción a su código HTTP (un solo lugar para los errores)
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400: falla un @Pattern / @Positive
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(HandlerMethodValidationException ex, HttpServletRequest req) {
        List<String> detalles = ex.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", detalles, req);
    }

    // 400: falta un parámetro obligatorio
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissing(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Falta un parámetro",
                List.of("Falta el parámetro '" + ex.getParameterName() + "'"), req);
    }

    // 400: valor con tipo incorrecto (ej: type=FANOUT o ttl=abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        Class<?> type = ex.getRequiredType();
        String detalle = (type != null && type.isEnum())
                ? ex.getName() + " debe ser uno de " + Arrays.toString(type.getEnumConstants())
                : ex.getName() + " tiene un valor inválido: '" + ex.getValue() + "'";
        return build(HttpStatus.BAD_REQUEST, "Tipo de dato inválido", List.of(detalle), req);
    }

    // 400: regla de negocio
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(InvalidRequestException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", List.of(ex.getMessage()), req);
    }

    // 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", List.of(ex.getMessage()), req);
    }

    // 409
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ResourceAlreadyExistsException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Recurso ya existe", List.of(ex.getMessage()), req);
    }

    // 503: RabbitMQ apagado o inalcanzable
    @ExceptionHandler(AmqpConnectException.class)
    public ResponseEntity<ErrorResponse> handleBrokerDown(AmqpConnectException ex, HttpServletRequest req) {
        log.error("[ERROR] RabbitMQ no disponible: {}", ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, "RabbitMQ no disponible",
                List.of("No se pudo conectar con RabbitMQ"), req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, List<String> detalles, HttpServletRequest req) {
        log.warn("[HTTP {}] {} {} -> {}", status.value(), req.getMethod(), req.getRequestURI(), detalles);
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status.value(), error, detalles, req.getRequestURI()));
    }
}
