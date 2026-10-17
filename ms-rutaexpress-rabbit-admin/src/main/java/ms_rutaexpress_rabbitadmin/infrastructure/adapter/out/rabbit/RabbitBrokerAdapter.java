package ms_rutaexpress_rabbitadmin.infrastructure.adapter.out.rabbit;

import ms_rutaexpress_rabbitadmin.domain.model.BindingSpec;
import ms_rutaexpress_rabbitadmin.domain.model.ExchangeType;
import ms_rutaexpress_rabbitadmin.domain.model.QueueSpec;
import ms_rutaexpress_rabbitadmin.domain.model.QueueStatus;
import ms_rutaexpress_rabbitadmin.domain.ports.out.BrokerAdminPort;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Adaptador de salida: aquí se usa RabbitAdmin (Guía 2.3.1)
@Component
public class RabbitBrokerAdapter implements BrokerAdminPort {

    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;

    public RabbitBrokerAdapter(RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Optional<QueueStatus> findQueue(String name) {
        QueueInformation info = rabbitAdmin.getQueueInfo(name);   // null si no existe
        return Optional.ofNullable(info)
                .map(i -> new QueueStatus(i.getName(), i.getMessageCount(), i.getConsumerCount()));
    }

    @Override
    public void declareQueue(QueueSpec spec) {
        QueueBuilder builder = QueueBuilder.durable(spec.name());
        // Solo se agregan los argumentos que vienen informados
        if (spec.ttl() != null) builder.ttl(spec.ttl().intValue());
        if (spec.maxLength() != null) builder.maxLength(spec.maxLength());
        if (spec.deadLetterExchange() != null) builder.deadLetterExchange(spec.deadLetterExchange());
        if (spec.deadLetterRoutingKey() != null) builder.deadLetterRoutingKey(spec.deadLetterRoutingKey());
        rabbitAdmin.declareQueue(builder.build());
    }

    @Override
    public void deleteQueue(String name) {
        rabbitAdmin.deleteQueue(name);
    }

    @Override
    public int purgeQueue(String name) {
        return rabbitAdmin.purgeQueue(name);
    }

    @Override
    public boolean exchangeExists(String name) {
        // RabbitAdmin no trae "getExchangeInfo": se consulta en modo pasivo (no crea, solo pregunta)
        try {
            return Boolean.TRUE.equals(rabbitTemplate.execute(channel -> {
                channel.exchangeDeclarePassive(name);
                return true;
            }));
        } catch (AmqpConnectException e) {
            throw e;          // RabbitMQ caído: no es "no existe"
        } catch (AmqpException e) {
            return false;     // 404 del broker: no existe
        }
    }

    @Override
    public void declareExchange(String name, ExchangeType type) {
        Exchange exchange = switch (type) {
            case DIRECT -> new DirectExchange(name, true, false);
            case TOPIC -> new TopicExchange(name, true, false);
        };
        rabbitAdmin.declareExchange(exchange);
    }

    @Override
    public void deleteExchange(String name) {
        rabbitAdmin.deleteExchange(name);
    }

    @Override
    public void declareBinding(BindingSpec spec) {
        rabbitAdmin.declareBinding(toBinding(spec));
    }

    @Override
    public void removeBinding(BindingSpec spec) {
        rabbitAdmin.removeBinding(toBinding(spec));
    }

    // Para enlazar solo importan los nombres y la key, no el tipo del exchange
    private Binding toBinding(BindingSpec spec) {
        return new Binding(spec.queueName(), Binding.DestinationType.QUEUE,
                spec.exchangeName(), spec.routingKey(), null);
    }
}
