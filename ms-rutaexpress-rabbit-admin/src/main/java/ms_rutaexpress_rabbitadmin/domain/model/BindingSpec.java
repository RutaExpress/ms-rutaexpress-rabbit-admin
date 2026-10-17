package ms_rutaexpress_rabbitadmin.domain.model;

// Regla exchange -> cola con su routing key
public record BindingSpec(
        String queueName,
        String exchangeName,
        String routingKey
) {}
