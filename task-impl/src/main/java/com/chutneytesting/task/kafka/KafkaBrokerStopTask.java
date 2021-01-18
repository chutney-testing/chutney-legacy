package com.chutneytesting.task.kafka;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

public class KafkaBrokerStopTask implements Task {

    private final Logger logger;
    private final EmbeddedKafkaBroker broker;

    public KafkaBrokerStopTask(Logger logger, @Input("broker") EmbeddedKafkaBroker broker) {
        this.logger = logger;
        this.broker = broker;
    }

    @Override
    public TaskExecutionResult execute() {
        logger.info("Call Kafka broker shutdown");
        broker.destroy();
        return TaskExecutionResult.ok();
    }
}
