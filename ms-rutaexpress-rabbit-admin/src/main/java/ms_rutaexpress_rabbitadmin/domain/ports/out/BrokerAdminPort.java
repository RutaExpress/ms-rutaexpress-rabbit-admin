package ms_rutaexpress_rabbitadmin.domain.ports.out;

import ms_rutaexpress_rabbitadmin.domain.model.BindingSpec;
import ms_rutaexpress_rabbitadmin.domain.model.ExchangeType;
import ms_rutaexpress_rabbitadmin.domain.model.QueueSpec;
import ms_rutaexpress_rabbitadmin.domain.model.QueueStatus;

import java.util.Optional;

// Puerto de salida: lo que necesitamos del broker (lo implementa el adaptador de RabbitMQ)
public interface BrokerAdminPort {

    Optional<QueueStatus> findQueue(String name);

    void declareQueue(QueueSpec spec);

    void deleteQueue(String name);

    int purgeQueue(String name);

    boolean exchangeExists(String name);

    void declareExchange(String name, ExchangeType type);

    void deleteExchange(String name);

    void declareBinding(BindingSpec spec);

    void removeBinding(BindingSpec spec);
}
