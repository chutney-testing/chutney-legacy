package com.chutneytesting.task.kafka;

import static java.util.Collections.emptyMap;
import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.TestTarget;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.Map;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.kafka.core.ConsumerFactory;

class KafkaConsumerFactoryFactoryTest {

    private final TestTarget.TestTargetBuilder targetWithoutProperties = TestTarget.TestTargetBuilder.builder()
        .withTargetId("kafka")
        .withUrl("tcp://127.0.0.1:5555");

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("should set kafka client bootstrap.servers configuration")
    class BootstrapServersConfig {

        @Test
        @Order(1)
        @DisplayName("from configuration")
        void should_use_configuration_for_bootstrap_servers_kafka_client_configuration() {
            Target target = targetWithoutProperties
                .withProperty(BOOTSTRAP_SERVERS_CONFIG, "target.host:6666")
                .build();

            Map<String, String> config = Map.of(BOOTSTRAP_SERVERS_CONFIG, "conf.host:9999");

            ConsumerFactory<String, String> consumerFactoryFactory = new KafkaConsumerFactoryFactory().create(target, "", config);

            assertThat(consumerFactoryFactory.getConfigurationProperties())
                .containsEntry(BOOTSTRAP_SERVERS_CONFIG, config.get(BOOTSTRAP_SERVERS_CONFIG));
        }

        @Test
        @Order(2)
        @DisplayName("from target properties")
        void should_use_target_properties_for_bootstrap_servers_kafka_client_configuration() {
            Target target = targetWithoutProperties
                .withProperty(BOOTSTRAP_SERVERS_CONFIG, "target.host:6666")
                .build();

            ConsumerFactory<String, String> consumerFactoryFactory = new KafkaConsumerFactoryFactory().create(target, "", emptyMap());

            assertThat(consumerFactoryFactory.getConfigurationProperties())
                .containsEntry(BOOTSTRAP_SERVERS_CONFIG, target.property(BOOTSTRAP_SERVERS_CONFIG).get());
        }

        @Test
        @Order(3)
        @DisplayName("from target's url authority")
        void should_use_target_authority_url_for_bootstrap_servers_kafka_client_configuration() {
            Target target = targetWithoutProperties.build();

            ConsumerFactory<String, String> consumerFactoryFactory = new KafkaConsumerFactoryFactory().create(target, "", emptyMap());

            assertThat(consumerFactoryFactory.getConfigurationProperties())
                .hasEntrySatisfying(BOOTSTRAP_SERVERS_CONFIG, (v) -> assertThat(v).isEqualTo("127.0.0.1:5555"));
        }

        @Test
        @Order(4)
        @DisplayName("from whole target's url otherwise")
        void should_use_target_host_url_for_bootstrap_servers_kafka_client_configuration() {
            Target target = targetWithoutProperties
                .withUrl("http:/a/path")
                .build();

            ConsumerFactory<String, String> consumerFactoryFactory = new KafkaConsumerFactoryFactory().create(target, "", emptyMap());

            assertThat(consumerFactoryFactory.getConfigurationProperties())
                .hasEntrySatisfying(BOOTSTRAP_SERVERS_CONFIG, (v) -> assertThat(v).isEqualTo(target.uri().toString()));
        }
    }

    @Test
    void should_use_StringDeserializer_as_key_and_value_deserializer() {
        Target target = targetWithoutProperties.build();

        ConsumerFactory<String, String> consumerFactoryFactory = new KafkaConsumerFactoryFactory().create(target, "", emptyMap());
        assertThat(consumerFactoryFactory.getKeyDeserializer())
            .isInstanceOf(StringDeserializer.class);
        assertThat(consumerFactoryFactory.getValueDeserializer())
            .isInstanceOf(StringDeserializer.class);
    }
}
