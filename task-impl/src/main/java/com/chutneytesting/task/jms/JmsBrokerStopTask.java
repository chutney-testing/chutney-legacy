package com.chutneytesting.task.jms;

import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.task.spi.validation.Validator.of;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.validation.Validator;
import java.util.List;
import java.util.Objects;
import org.apache.activemq.broker.BrokerService;

public class JmsBrokerStopTask implements Task {

    private final Logger logger;
    private final BrokerService brokerService;

    public JmsBrokerStopTask(Logger logger, @Input("jms-broker-service") BrokerService brokerService) {
        this.logger = logger;
        this.brokerService = brokerService;
    }

    @Override
    public List<String> validateInputs() {
        Validator<BrokerService> jmsBrokerValidation = of(brokerService)
            .validate(Objects::nonNull, "No jms-broker-service provided");
        return getErrorsFrom(jmsBrokerValidation);
    }

    @Override
    public TaskExecutionResult execute() {
        logger.info("Call jms broker shutdown");
        try {
            brokerService.stop();
            return TaskExecutionResult.ok();
        } catch (Exception e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }
}
