package com.chutneytesting.task.jms;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import org.apache.activemq.broker.BrokerService;

public class JmsBrokerStopTask implements Task {

    private final Logger logger;
    private final BrokerService brokerService;

    public JmsBrokerStopTask(Logger logger, @Input("jms-broker-service") BrokerService brokerService) {
        this.logger = logger;
        this.brokerService = brokerService;
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
