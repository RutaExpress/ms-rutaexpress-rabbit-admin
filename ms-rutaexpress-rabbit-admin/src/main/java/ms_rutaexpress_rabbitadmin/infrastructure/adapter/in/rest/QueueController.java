package ms_rutaexpress_rabbitadmin.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import ms_rutaexpress_rabbitadmin.domain.model.QueueSpec;
import ms_rutaexpress_rabbitadmin.domain.model.QueueStatus;
import ms_rutaexpress_rabbitadmin.domain.ports.in.RabbitAdminService;
import ms_rutaexpress_rabbitadmin.infrastructure.adapter.in.rest.dto.MessageResponse;
import ms_rutaexpress_rabbitadmin.infrastructure.adapter.in.rest.dto.PurgeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static ms_rutaexpress_rabbitadmin.infrastructure.adapter.in.rest.validation.ValidationRules.*;

// Endpoints de colas (rutas de la Guía 2.3.1)
@RestController
@RequestMapping("/api/rabbitmq/queues")
public class QueueController {

    private final RabbitAdminService adminService;

    public QueueController(RabbitAdminService adminService) {
        this.adminService = adminService;
    }

    // POST /api/rabbitmq/queues?queueName=...&ttl=...&maxLength=...&deadLetterExchange=...&deadLetterRoutingKey=...
    @PostMapping
    public ResponseEntity<QueueStatus> createQueue(
            @RequestParam @Pattern(regexp = NAME_REGEX, message = "queueName: " + NAME_MESSAGE) String queueName,
            @RequestParam(required = false) @Positive(message = "ttl debe ser mayor a 0 (ms)") Long ttl,
            @RequestParam(required = false) @Positive(message = "maxLength debe ser mayor a 0") Integer maxLength,
            @RequestParam(required = false) @Pattern(regexp = NAME_REGEX, message = "deadLetterExchange: " + NAME_MESSAGE) String deadLetterExchange,
            @RequestParam(required = false) @Pattern(regexp = ROUTING_KEY_REGEX, message = "deadLetterRoutingKey: " + ROUTING_KEY_MESSAGE) String deadLetterRoutingKey) {

        QueueSpec spec = new QueueSpec(queueName, ttl, maxLength, deadLetterExchange, deadLetterRoutingKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createQueue(spec));   // 201
    }

    // GET /api/rabbitmq/queues/{queueName} -> mensajes y consumidores
    @GetMapping("/{queueName}")
    public ResponseEntity<QueueStatus> getQueue(@PathVariable String queueName) {
        return ResponseEntity.ok(adminService.getQueue(queueName));
    }

    // DELETE /api/rabbitmq/queues/{queueName}
    @DeleteMapping("/{queueName}")
    public ResponseEntity<MessageResponse> deleteQueue(@PathVariable String queueName) {
        adminService.deleteQueue(queueName);
        return ResponseEntity.ok(new MessageResponse("Cola '" + queueName + "' eliminada"));
    }

    // POST /api/rabbitmq/queues/{queueName}/purge -> vacía la cola (ej: una DLQ ya revisada)
    @PostMapping("/{queueName}/purge")
    public ResponseEntity<PurgeResponse> purgeQueue(@PathVariable String queueName) {
        int purged = adminService.purgeQueue(queueName);
        return ResponseEntity.ok(new PurgeResponse(queueName, purged));
    }
}
