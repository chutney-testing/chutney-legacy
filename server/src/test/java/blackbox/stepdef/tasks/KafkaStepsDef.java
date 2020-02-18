package blackbox.stepdef.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

public class KafkaStepsDef {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStepsDef.class);

    private KafkaEmbedded embeddedKafka;
    private KafkaMessageListenerContainer<String, String> container;
    private BlockingQueue<ConsumerRecord<String, String>> records;

    public KafkaStepsDef() {
    }

    @After
    public void tearDown() throws InterruptedException {
        if (container != null) {
            container.stop();
        }
        if (embeddedKafka != null) {
            embeddedKafka.after();
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    @Given("^an embedded kafka server with a topic (.+)")
    public void an_kafka_server_is_started(String topic) throws Exception {
        embeddedKafka = new KafkaEmbedded(1, true, topic);
        embeddedKafka.before();
    }


    @And("^a consumer listening the kafka topic (.+)")
    public void a_consumer_listening_the_kafka_topic(String topic) throws Exception {
        Map<String, Object> consumerProperties =
            KafkaTestUtils.consumerProps("sender", "false", embeddedKafka);

        DefaultKafkaConsumerFactory<String, String> consumerFactory =
            new DefaultKafkaConsumerFactory<>(consumerProperties);

        ContainerProperties containerProperties = new ContainerProperties(topic);
        containerProperties.setShutdownTimeout(500L);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();

        container.setupMessageListener((MessageListener<String, String>) record -> {
            LOGGER.info("kafka test-listener received message="+record.toString());
            records.add(record);
        });

        container.start();

        // wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
    }

    @And("^the message payload (.+) is well produced$")
    public void the_message_payload_is_well_produced(String payload) throws Throwable {
        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.value()).isEqualTo(payload);
    }

}
