package ms_rutaexpress_rabbitadmin.application.service;

import lombok.extern.slf4j.Slf4j;
import ms_rutaexpress_rabbitadmin.domain.exception.InvalidRequestException;
import ms_rutaexpress_rabbitadmin.domain.exception.ResourceAlreadyExistsException;
import ms_rutaexpress_rabbitadmin.domain.exception.ResourceNotFoundException;
import ms_rutaexpress_rabbitadmin.domain.model.BindingSpec;
import ms_rutaexpress_rabbitadmin.domain.model.ExchangeType;
import ms_rutaexpress_rabbitadmin.domain.model.QueueSpec;
import ms_rutaexpress_rabbitadmin.domain.model.QueueStatus;
import ms_rutaexpress_rabbitadmin.domain.ports.in.RabbitAdminService;
import ms_rutaexpress_rabbitadmin.domain.ports.out.BrokerAdminPort;
import org.springframework.stereotype.Service;

// Reglas de negocio del admin: verifica existencia antes de crear/borrar
@Slf4j
@Service
public class RabbitAdminServiceImpl implements RabbitAdminService {

    private final BrokerAdminPort broker;

    public RabbitAdminServiceImpl(BrokerAdminPort broker) {
        this.broker = broker;
    }

    // ---------- Colas ----------

    @Override
    public QueueStatus createQueue(QueueSpec spec) {
        if (broker.findQueue(spec.name()).isPresent()) {
            throw new ResourceAlreadyExistsException("La cola '" + spec.name() + "' ya existe");
        }
        validateDeadLetter(spec);
        broker.declareQueue(spec);
        log.info("[COLA-CREADA] {} ttl={} maxLength={} dlx={} dlk={}", spec.name(), spec.ttl(),
                spec.maxLength(), spec.deadLetterExchange(), spec.deadLetterRoutingKey());
        return getQueue(spec.name());
    }

    @Override
    public QueueStatus getQueue(String name) {
        return broker.findQueue(name)
                .orElseThrow(() -> new ResourceNotFoundException("La cola '" + name + "' no existe"));
    }

    @Override
    public void deleteQueue(String name) {
        getQueue(name);   // 404 si no existe
        broker.deleteQueue(name);
        log.info("[COLA-ELIMINADA] {}", name);
    }

    @Override
    public int purgeQueue(String name) {
        getQueue(name);
        int purged = broker.purgeQueue(name);
        log.info("[COLA-PURGADA] {} mensajes eliminados={}", name, purged);
        return purged;
    }

    // ---------- Exchanges ----------

    @Override
    public void createExchange(String name, ExchangeType type) {
        if (broker.exchangeExists(name)) {
            throw new ResourceAlreadyExistsException("El exchange '" + name + "' ya existe");
        }
        broker.declareExchange(name, type);
        log.info("[EXCHANGE-CREADO] {} tipo={}", name, type);
    }

    @Override
    public void deleteExchange(String name) {
        requireExchange(name);
        broker.deleteExchange(name);   // RabbitMQ borra también sus bindings
        log.info("[EXCHANGE-ELIMINADO] {}", name);
    }

    // ---------- Bindings ----------

    @Override
    public void createBinding(BindingSpec spec) {
        getQueue(spec.queueName());
        requireExchange(spec.exchangeName());
        broker.declareBinding(spec);
        log.info("[BINDING-CREADO] {} --({})--> {}", spec.exchangeName(), spec.routingKey(), spec.queueName());
    }

    @Override
    public void deleteBinding(BindingSpec spec) {
        getQueue(spec.queueName());
        requireExchange(spec.exchangeName());
        broker.removeBinding(spec);
        log.info("[BINDING-ELIMINADO] {} --({})--> {}", spec.exchangeName(), spec.routingKey(), spec.queueName());
    }

    // ---------- Reglas privadas ----------

    private void requireExchange(String name) {
        if (!broker.exchangeExists(name)) {
            throw new ResourceNotFoundException("El exchange '" + name + "' no existe");
        }
    }

    // La DLK sin DLX no sirve, y la DLX debe existir para no perder los mensajes fallidos
    private void validateDeadLetter(QueueSpec spec) {
        if (spec.deadLetterRoutingKey() != null && spec.deadLetterExchange() == null) {
            throw new InvalidRequestException("deadLetterRoutingKey requiere deadLetterExchange");
        }
        if (spec.deadLetterExchange() != null) {
            requireExchange(spec.deadLetterExchange());
        }
    }
}
