# ms-rutaexpress-rabbit-admin

Microservicio de **administración de RabbitMQ** del caso RutaExpress 
Permite crear, consultar, purgar y eliminar **colas, exchanges y bindings** en tiempo de ejecución mediante endpoints REST, usando `RabbitAdmin` de Spring AMQP 

## Tecnologías
- Java 21 · Spring Boot 4.1.1 · Spring AMQP (`RabbitAdmin`) · Bean Validation · Lombok
- RabbitMQ 4.2 (contenedor `rutaexpress-rabbitmq` de `docker-compose.infra.yml`)

## Estructura (arquitectura hexagonal)
```
ms-rutaexpress-rabbit-admin/src/main/java/ms_rutaexpress_rabbitadmin/
├── domain/
│   ├── model/        ExchangeType (enum), QueueSpec, QueueStatus, BindingSpec (records)
│   ├── exception/    ResourceNotFoundException (404), ResourceAlreadyExistsException (409), InvalidRequestException (400)
│   └── ports/
│       ├── in/       RabbitAdminService      ← lo que ofrece el microservicio
│       └── out/      BrokerAdminPort         ← lo que necesita del broker
├── application/
│   └── service/      RabbitAdminServiceImpl  ← reglas: existe / no existe / DLX válida
└── infrastructure/
    ├── config/       RabbitAdminConfig       ← beans RabbitAdmin y RabbitTemplate
    └── adapter/
        ├── in/rest/  QueueController, ExchangeController, BindingController,
        │             GlobalExceptionHandler, dto/, validation/ValidationRules
        └── out/rabbit/ RabbitBrokerAdapter   ← aquí se usa RabbitAdmin
```

## Ejecución local
1. Levantar RabbitMQ: `docker compose -f docker-compose.infra.yml up -d`
2. Desde `ms-rutaexpress-rabbit-admin/`: `mvnw.cmd spring-boot:run`
3. API en `http://localhost:8085/api/rabbitmq`

## Variables de entorno
| Variable | Por defecto |
|---|---|
| `RABBITMQ_HOST` | localhost |
| `RABBITMQ_PORT` | 5672 |
| `RABBITMQ_USER` | guest |
| `RABBITMQ_PASSWORD` | guest |

## Endpoints (`/api/rabbitmq`)
| Método | Ruta | Parámetros | Respuesta |
|---|---|---|---|
| POST | `/queues` | `queueName` · opcionales `ttl`, `maxLength`, `deadLetterExchange`, `deadLetterRoutingKey` | 201 / 400 / 404 (DLX no existe) / 409 |
| GET | `/queues/{queueName}` | — | 200 (mensajes y consumidores) / 404 |
| DELETE | `/queues/{queueName}` | — | 200 / 404 |
| POST | `/queues/{queueName}/purge` | — | 200 (mensajes eliminados) / 404 |
| POST | `/exchanges` | `exchangeName`, `type` = DIRECT \| TOPIC (por defecto DIRECT) | 201 / 400 / 409 |
| DELETE | `/exchanges/{exchangeName}` | — | 200 / 404 |
| POST | `/bindings` | `queueName`, `exchangeName`, `routingKey` | 201 / 400 / 404 |
| DELETE | `/bindings` | `queueName`, `exchangeName`, `routingKey` | 200 / 404 |

Si RabbitMQ no está disponible → **503**.

## Validaciones de entrada
- Nombres: letras, números, `.`, `-`, `_` (máx. 200) y no pueden empezar con `amq.` (reservado por RabbitMQ).
- Routing key: además acepta los comodines `*` y `#`.
- `ttl` y `maxLength` deben ser mayores a 0.
- `deadLetterRoutingKey` requiere `deadLetterExchange`, y la DLX debe existir.

## Avance
| Paso | Descripción | Estado |
|---|---|---|
| E1 | Base del proyecto, configuración y README | ✅ |
| E2 | Colas: crear, consultar, eliminar, purgar | ✅ |
| E3 | Exchanges y bindings | ✅ |
| E4 | Validación y manejo de errores | ✅ |