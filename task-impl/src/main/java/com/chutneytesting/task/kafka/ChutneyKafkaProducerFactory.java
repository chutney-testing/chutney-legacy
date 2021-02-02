package com.chutneytesting.task.kafka;

import com.chutneytesting.task.spi.injectable.Target;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;

final class ChutneyKafkaProducerFactory {

    private DefaultKafkaProducerFactory<String, String> factory;

    ProducerFactory<String, String> create(Target target) {
        Map<String, Object> configProps = new HashMap<>();

        configProps.put(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            target.url());

        configProps.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class);
        configProps.put(
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class);

        this.factory = new DefaultKafkaProducerFactory<>(configProps);

        return this.factory;
    }

    void destroy() throws Exception {
        factory.destroy();
    }
}
