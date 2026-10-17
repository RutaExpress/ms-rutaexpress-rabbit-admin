package ms_rutaexpress_rabbitadmin.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.Pattern;
import ms_rutaexpress_rabbitadmin.domain.model.ExchangeType;
import ms_rutaexpress_rabbitadmin.domain.ports.in.RabbitAdminService;
import ms_rutaexpress_rabbitadmin.infrastructure.adapter.in.rest.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static ms_rutaexpress_rabbitadmin.infrastructure.adapter.in.rest.validation.ValidationRules.*;

// Endpoints de exchanges
@RestController
@RequestMapping("/api/rabbitmq/exchanges")
public class ExchangeController {

    private final RabbitAdminService adminService;

    public ExchangeController(RabbitAdminService adminService) {
        this.adminService = adminService;
    }

    // POST /api/rabbitmq/exchanges?exchangeName=...&type=DIRECT|TOPIC (type por defecto DIRECT)
    @PostMapping
    public ResponseEntity<MessageResponse> createExchange(
            @RequestParam @Pattern(regexp = NAME_REGEX, message = "exchangeName: " + NAME_MESSAGE) String exchangeName,
            @RequestParam(defaultValue = "DIRECT") ExchangeType type) {

        adminService.createExchange(exchangeName, type);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Exchange '" + exchangeName + "' (" + type + ") creado"));
    }

    // DELETE /api/rabbitmq/exchanges/{exchangeName}
    @DeleteMapping("/{exchangeName}")
    public ResponseEntity<MessageResponse> deleteExchange(@PathVariable String exchangeName) {
        adminService.deleteExchange(exchangeName);
        return ResponseEntity.ok(new MessageResponse("Exchange '" + exchangeName + "' eliminado"));
    }
}
