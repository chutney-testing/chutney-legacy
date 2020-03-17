package com.chutneytesting.task.kafka;

import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;

import com.chutneytesting.task.spi.injectable.Target;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

public class KafkaConsumerFactoryFactory {

    public ConsumerFactory<String, String> create(Target target, String group, Map<String, String> properties) {

        Map<String, Object> consumerConfig = ImmutableMap.<String, Object>builder()
            .put(BOOTSTRAP_SERVERS_CONFIG, target.url())
            .put(GROUP_ID_CONFIG, group)
            .putAll(properties)
            .build();

        DefaultKafkaConsumerFactory<String, String> kafkaConsumerFactory =
            new DefaultKafkaConsumerFactory<>(
                consumerConfig,
                new StringDeserializer(),
                new StringDeserializer());
        return kafkaConsumerFactory;
    }
}
