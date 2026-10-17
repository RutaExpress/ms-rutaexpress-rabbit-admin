package ms_rutaexpress_rabbitadmin.infrastructure.adapter.in.rest.dto;

// Respuesta del purge: cuántos mensajes se borraron
public record PurgeResponse(String cola, int mensajesEliminados) {}
