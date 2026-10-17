package ms_rutaexpress_rabbitadmin.domain.model;

// Tipos de exchange que permite crear el admin (los del caso)
public enum ExchangeType {
    DIRECT,   // key exacta
    TOPIC     // patrón con * y #
}
