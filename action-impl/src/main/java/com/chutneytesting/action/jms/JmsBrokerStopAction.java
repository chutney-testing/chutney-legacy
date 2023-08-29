package com.chutneytesting.action.jms;

import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.action.spi.validation.Validator.of;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.validation.Validator;
import java.util.List;
import java.util.Objects;
import org.apache.activemq.broker.BrokerService;

public class JmsBrokerStopAction implements Action {

    private final Logger logger;
    private final BrokerService brokerService;

    public JmsBrokerStopAction(Logger logger, @Input("jms-broker-service") BrokerService brokerService) {
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
    public ActionExecutionResult execute() {
        logger.info("Call jms broker shutdown");
        try {
            brokerService.stop();
            return ActionExecutionResult.ok();
        } catch (Exception e) {
            logger.error(e);
            return ActionExecutionResult.ko();
        }
    }
}
