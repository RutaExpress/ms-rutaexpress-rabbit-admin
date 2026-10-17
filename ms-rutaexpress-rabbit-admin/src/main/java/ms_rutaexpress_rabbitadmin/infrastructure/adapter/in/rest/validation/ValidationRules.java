package ms_rutaexpress_rabbitadmin.infrastructure.adapter.in.rest.validation;

// Reglas de validación de entrada centralizadas (se usan en los @Pattern)
public final class ValidationRules {

    private ValidationRules() {}

    // Letras, números, punto, guion y guion bajo. No puede empezar con "amq." (reservado de RabbitMQ)
    public static final String NAME_REGEX = "^(?!amq\\.)[a-zA-Z0-9._-]{1,200}$";
    public static final String NAME_MESSAGE =
            "solo letras, números, '.', '-', '_' (máx. 200) y no puede empezar con 'amq.'";

    // Igual que el nombre, pero acepta los comodines * y # del topic
    public static final String ROUTING_KEY_REGEX = "^[a-zA-Z0-9._*#-]{1,200}$";
    public static final String ROUTING_KEY_MESSAGE =
            "solo letras, números, '.', '-', '_', '*', '#' (máx. 200)";
}
