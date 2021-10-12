package com.chutneytesting.task.kafka;

import com.chutneytesting.task.spi.injectable.Target;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

final class ChutneyKafkaProducerFactory {

    private DefaultKafkaProducerFactory<String, String> factory;

    KafkaTemplate<String, String> create(Target target) {
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

        target.properties().entrySet().forEach(p -> configProps.put(p.getKey(), p.getValue()));

        this.factory = new DefaultKafkaProducerFactory<>(configProps);
        return new KafkaTemplate(this.factory, true);
    }

    void destroy() {
        factory.destroy();
    }
}
