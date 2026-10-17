package ms_rutaexpress_rabbitadmin.domain.ports.in;

import ms_rutaexpress_rabbitadmin.domain.model.BindingSpec;
import ms_rutaexpress_rabbitadmin.domain.model.ExchangeType;
import ms_rutaexpress_rabbitadmin.domain.model.QueueSpec;
import ms_rutaexpress_rabbitadmin.domain.model.QueueStatus;

// Puerto de entrada: operaciones de administración que exponen los controllers
public interface RabbitAdminService {

    QueueStatus createQueue(QueueSpec spec);

    QueueStatus getQueue(String name);

    void deleteQueue(String name);

    int purgeQueue(String name);

    void createExchange(String name, ExchangeType type);

    void deleteExchange(String name);

    void createBinding(BindingSpec spec);

    void deleteBinding(BindingSpec spec);
}
