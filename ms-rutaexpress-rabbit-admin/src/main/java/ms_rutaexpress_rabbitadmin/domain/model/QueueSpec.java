package ms_rutaexpress_rabbitadmin.domain.model;

// Datos para crear una cola. Los campos opcionales pueden venir en null
public record QueueSpec(
        String name,
        Long ttl,                     // x-message-ttl en ms (opcional)
        Integer maxLength,            // x-max-length (opcional)
        String deadLetterExchange,    // x-dead-letter-exchange (opcional)
        String deadLetterRoutingKey   // x-dead-letter-routing-key (opcional)
) {}
