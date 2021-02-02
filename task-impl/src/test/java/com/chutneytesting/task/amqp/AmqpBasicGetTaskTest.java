package com.chutneytesting.task.amqp;

import static com.chutneytesting.task.amqp.AmqpTasksTest.mockConnectionFactory;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.TaskExecutionResult.Status;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.TestTarget;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;

public class AmqpBasicGetTaskTest {

    @Test
    public void basicGet_fails_when_no_message_is_available() throws IOException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        MockConnectionFactory mockConnectionFactory = new MockConnectionFactory();
        TestLogger logger = new TestLogger();
        Target target = TestTarget.TestTargetBuilder.builder()
            .withTargetId("rabbit")
            .withUrl("amqp://non_host:1234")
            .withSecurity("guest", "guest")
            .build();

        String queueName = mockConnectionFactory.newConnection().createChannel().queueDeclare().getQueue();

        Task amqpBasicGetTask = mockConnectionFactory(new AmqpBasicGetTask(target, queueName, logger), mockConnectionFactory);

        TaskExecutionResult taskExecutionResult = amqpBasicGetTask.execute();

        assertThat(taskExecutionResult.status).isEqualTo(Status.Failure);
        assertThat(logger.errors).containsOnly("No message available");
    }
}
