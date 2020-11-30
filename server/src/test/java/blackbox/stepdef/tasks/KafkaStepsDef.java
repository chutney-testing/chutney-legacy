package blackbox.stepdef.tasks;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import java.util.concurrent.TimeUnit;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

public class KafkaStepsDef {

    private EmbeddedKafkaBroker embeddedKafka;

    public KafkaStepsDef() {
    }

    @After
    public void tearDown() throws InterruptedException {
        if (embeddedKafka != null) {
            embeddedKafka.destroy();
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    @Given("^an embedded kafka server with a topic (.+)")
    public void an_kafka_server_is_started(String topic) throws Exception {
        embeddedKafka = new EmbeddedKafkaBroker(1, true, topic);
        embeddedKafka.afterPropertiesSet();
    }
}
