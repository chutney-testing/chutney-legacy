package com.chutneytesting.action.kafka;

import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.action.spi.validation.Validator.of;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.validation.Validator;
import java.util.List;
import java.util.Objects;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

public class KafkaBrokerStopAction implements Action {

    private final Logger logger;
    private final EmbeddedKafkaBroker broker;

    public KafkaBrokerStopAction(Logger logger, @Input("broker") EmbeddedKafkaBroker broker) {
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
    public ActionExecutionResult execute() {
        logger.info("Call Kafka broker shutdown");
        broker.destroy();
        return ActionExecutionResult.ok();
    }
}
