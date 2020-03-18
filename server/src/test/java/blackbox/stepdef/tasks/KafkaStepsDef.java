package blackbox.stepdef.tasks;

import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.test.rule.KafkaEmbedded;

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

}
