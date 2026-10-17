package ms_rutaexpress_rabbitadmin.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.Pattern;
import ms_rutaexpress_rabbitadmin.domain.model.BindingSpec;
import ms_rutaexpress_rabbitadmin.domain.ports.in.RabbitAdminService;
import ms_rutaexpress_rabbitadmin.infrastructure.adapter.in.rest.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static ms_rutaexpress_rabbitadmin.infrastructure.adapter.in.rest.validation.ValidationRules.*;

// Endpoints de bindings (exchange -> cola con routing key)
@RestController
@RequestMapping("/api/rabbitmq/bindings")
public class BindingController {

    private final RabbitAdminService adminService;

    public BindingController(RabbitAdminService adminService) {
        this.adminService = adminService;
    }

    // POST /api/rabbitmq/bindings?queueName=...&exchangeName=...&routingKey=...
    @PostMapping
    public ResponseEntity<MessageResponse> createBinding(
            @RequestParam @Pattern(regexp = NAME_REGEX, message = "queueName: " + NAME_MESSAGE) String queueName,
            @RequestParam @Pattern(regexp = NAME_REGEX, message = "exchangeName: " + NAME_MESSAGE) String exchangeName,
            @RequestParam @Pattern(regexp = ROUTING_KEY_REGEX, message = "routingKey: " + ROUTING_KEY_MESSAGE) String routingKey) {

        adminService.createBinding(new BindingSpec(queueName, exchangeName, routingKey));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Binding creado: " + exchangeName + " --(" + routingKey + ")--> " + queueName));
    }

    // DELETE /api/rabbitmq/bindings?queueName=...&exchangeName=...&routingKey=...
    @DeleteMapping
    public ResponseEntity<MessageResponse> deleteBinding(
            @RequestParam @Pattern(regexp = NAME_REGEX, message = "queueName: " + NAME_MESSAGE) String queueName,
            @RequestParam @Pattern(regexp = NAME_REGEX, message = "exchangeName: " + NAME_MESSAGE) String exchangeName,
            @RequestParam @Pattern(regexp = ROUTING_KEY_REGEX, message = "routingKey: " + ROUTING_KEY_MESSAGE) String routingKey) {

        adminService.deleteBinding(new BindingSpec(queueName, exchangeName, routingKey));
        return ResponseEntity.ok(new MessageResponse("Binding eliminado: " + exchangeName + " --(" + routingKey + ")--> " + queueName));
    }
}
