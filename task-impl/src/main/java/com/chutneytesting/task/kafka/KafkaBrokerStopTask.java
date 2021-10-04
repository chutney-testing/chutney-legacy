package com.chutneytesting.task.kafka;

import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.task.spi.validation.Validator.of;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.validation.Validator;
import java.util.List;
import java.util.Objects;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

public class KafkaBrokerStopTask implements Task {

    private final Logger logger;
    private final EmbeddedKafkaBroker broker;

    public KafkaBrokerStopTask(Logger logger, @Input("broker") EmbeddedKafkaBroker broker) {
        this.logger = logger;
        this.broker = broker;
    }

    @Override
    public List<String> validateInputs() {
        Validator<EmbeddedKafkaBroker> embeddedKafkaBrokerValidation = of(broker)
            .validate(Objects::nonNull, "No broker provided");
        return getErrorsFrom(embeddedKafkaBrokerValidation);
    }

    @Override
    public TaskExecutionResult execute() {
        logger.info("Call Kafka broker shutdown");
        broker.destroy();
        return TaskExecutionResult.ok();
    }
}
