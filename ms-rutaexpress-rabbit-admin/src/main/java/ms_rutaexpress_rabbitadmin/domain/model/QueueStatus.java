package ms_rutaexpress_rabbitadmin.domain.model;

// Estado de una cola: lo que responde GET /queues/{name}
public record QueueStatus(
        String name,
        long messageCount,   // mensajes esperando (Ready)
        int consumerCount    // consumidores conectados
) {}
