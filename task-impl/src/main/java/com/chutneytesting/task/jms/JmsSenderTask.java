package com.chutneytesting.task.jms;

import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.targetValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.jms.JMSException;

public class JmsSenderTask implements Task {

    private final Target target;
    private final Logger logger;

    private final String destination;
    private final String body;
    private final Map<String, String> headers;

    // TODO create injectable service
    private final JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory();

    public JmsSenderTask(Target target, Logger logger, @Input("destination") String destination, @Input("body") String body, @Input("headers") Map<String, String> headers) {
        this.target = target;
        this.logger = logger;
        this.destination = destination;
        this.body = body;
        this.headers = headers != null ? headers : Collections.emptyMap();
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            targetValidation(target),
            notBlankStringValidation(destination, "destination"),
            notBlankStringValidation(body, "body")
        );
    }

    @Override
    public TaskExecutionResult execute() {
        try (CloseableResource<JmsConnectionFactory.MessageSender> producer = jmsConnectionFactory.getMessageProducer(target, destination)) {
            producer.getResource().send(body, headers);
            logger.info("Successfully sent message on " + destination + " to " + target.name() + " (" + target.url() + ")");
            return TaskExecutionResult.ok();
        } catch (JMSException | UncheckedJmsException e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }

}
