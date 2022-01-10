package com.chutneytesting.task.kafka;

import static com.chutneytesting.task.kafka.KafkaClientFactoryHelper.resolveBootStrapServerConfig;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;

import com.chutneytesting.task.spi.injectable.Target;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

final class ChutneyKafkaProducerFactory {

    private DefaultKafkaProducerFactory<String, String> factory;

    KafkaTemplate<String, String> create(Target target, Map<String, String> config) {

        Map<String, Object> producerConfig = new HashMap<>();
        producerConfig.put(BOOTSTRAP_SERVERS_CONFIG, resolveBootStrapServerConfig(target));
        producerConfig.putAll(config);

        this.factory = new DefaultKafkaProducerFactory<>(
            producerConfig,
            new StringSerializer(),
            new StringSerializer());

        return new KafkaTemplate<>(this.factory, true);
    }

    void destroy() {
        factory.destroy();
    }
}
