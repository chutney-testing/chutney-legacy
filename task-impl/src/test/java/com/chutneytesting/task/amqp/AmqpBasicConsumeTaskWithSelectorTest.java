package com.chutneytesting.task.amqp;

import static com.chutneytesting.task.amqp.AmqpTasksTest.mockConnectionFactory;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.TaskExecutionResult.Status;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.TestFinallyActionRegistry;
import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.TestTarget;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import wiremock.com.google.common.collect.ImmutableMap;

public class AmqpBasicConsumeTaskWithSelectorTest {

    @Test
    public void basicConsume_filter_messages_with_selector() throws IOException {
        MockConnectionFactory mockConnectionFactory = new MockConnectionFactory();
        TestLogger logger = new TestLogger();

        Target target = TestTarget.TestTargetBuilder.builder()
            .withTargetId("rabbit")
            .withUrl("amqp://non_host:1234")
            .withSecurity("guest", "guest")
            .build();

        mockConnectionFactory.newConnection().createChannel().exchangeDeclare("test-ex", "fanout");

        String queueName = "toto";
        Task amqpCreateBoundTemporaryQueueTask = mockConnectionFactory(new AmqpCreateBoundTemporaryQueueTask(
            target,
            "test-ex",
            "test",
            queueName,
            logger,
            new TestFinallyActionRegistry()
        ), mockConnectionFactory);

        TaskExecutionResult amqpCreateBoundTemporaryQueueResult = amqpCreateBoundTemporaryQueueTask.execute();
        assertThat(amqpCreateBoundTemporaryQueueResult.status).isEqualTo(Status.Success);

        Task amqpBasicPublishTask1 = mockConnectionFactory(new AmqpBasicPublishTask(
            target,
            "test-ex",
            "test",
            ImmutableMap.of("maVersion", "1.0", "timestamp", "987456321"),
            Collections.singletonMap("content_type", "application/json"),
            "{\"value\": \"test message\", \"id\": \"7777\"}",
            logger
        ), mockConnectionFactory);
        assertThat(amqpBasicPublishTask1.execute().status).isEqualTo(TaskExecutionResult.Status.Success);
        assertThat(amqpBasicPublishTask1.execute().status).isEqualTo(TaskExecutionResult.Status.Success);
        Task amqpBasicPublishTask2 = mockConnectionFactory(new AmqpBasicPublishTask(
            target,
            "test-ex",
            "test",
            ImmutableMap.of("maVersion", "2.0", "timestamp", "987456321"),
            Collections.singletonMap("content_type", "application/json"),
            "{\"value\": \"test message\", \"id\": \"1111\", \"item\": {\"id\": \"123\"}   }",
            logger
        ), mockConnectionFactory);
        assertThat(amqpBasicPublishTask2.execute().status).isEqualTo(TaskExecutionResult.Status.Success);

        Task amqpBasicConsumeTask = mockConnectionFactory(new AmqpBasicConsumeTask(
            target,
            queueName,
            1,
            "$..[?($.headers.maVersion=='2.0' && $.payload.item.id==\"123\")]",
            "10 sec",
            logger
        ), mockConnectionFactory);

        TaskExecutionResult amqpBasicConsumeResult = amqpBasicConsumeTask.execute();
        assertThat(amqpBasicConsumeResult.status).isEqualTo(Status.Success);
        assertThat(amqpBasicConsumeResult.outputs).hasSize(3);
        final List<Map<String, Object>> body = (List<Map<String, Object>>) amqpBasicConsumeResult.outputs.get("body");
        final List<Map<String, Object>> payloads = (List<Map<String, Object>>) amqpBasicConsumeResult.outputs.get("payloads");
        final List<Map<String, Object>> headers = (List<Map<String, Object>>) amqpBasicConsumeResult.outputs.get("headers");
        assertThat(body.size()).isEqualTo(1);
        final Map<String, Object> message = body.get(0);
        final Map<String, Object> payload1 = (Map<String, Object>) message.get("payload");
        assertThat(payload1.get("value")).isEqualTo("test message");
        assertThat(payload1.get("id")).isEqualTo("1111");
        final Map<String, Object> headers1 = (Map<String, Object>) message.get("headers");
        assertThat(headers1.get("maVersion")).isEqualTo("2.0");
        assertThat(payload1).isEqualTo(payloads.get(0));
        assertThat(headers1).isEqualTo(headers.get(0));

    }

    @Test
    public void basicConsume_filter_messages_with_selector_return_not_expected_messages() throws IOException {
        MockConnectionFactory mockConnectionFactory = new MockConnectionFactory();
        TestLogger logger = new TestLogger();

        Target target = TestTarget.TestTargetBuilder.builder()
            .withTargetId("rabbit")
            .withUrl("amqp://non_host:1234")
            .withSecurity("guest", "guest")
            .build();

        mockConnectionFactory.newConnection().createChannel().exchangeDeclare("test-ex", "fanout");

        String queueName = "toto";
        Task amqpCreateBoundTemporaryQueueTask = mockConnectionFactory(new AmqpCreateBoundTemporaryQueueTask(
            target,
            "test-ex",
            "test",
            queueName,
            logger,
            new TestFinallyActionRegistry()
        ), mockConnectionFactory);

        TaskExecutionResult amqpCreateBoundTemporaryQueueResult = amqpCreateBoundTemporaryQueueTask.execute();
        assertThat(amqpCreateBoundTemporaryQueueResult.status).isEqualTo(Status.Success);

        Task amqpBasicPublishTask = mockConnectionFactory(new AmqpBasicPublishTask(
            target,
            "test-ex",
            "test",
            ImmutableMap.of("maVersion", "1.0", "timestamp", "987456321"),
            Collections.singletonMap("content_type", "application/json"),
            "{\"value\": \"test message\", \"id\": \"7777\"}",
            logger
        ), mockConnectionFactory);
        assertThat(amqpBasicPublishTask.execute().status).isEqualTo(TaskExecutionResult.Status.Success);

        Task amqpBasicConsumeTask = mockConnectionFactory(new AmqpBasicConsumeTask(
            target,
            queueName,
            1,
            "$..[?($.headers.maVersion=='3.0')]",
            "10 sec",
            logger
        ), mockConnectionFactory);

        TaskExecutionResult amqpBasicConsumeResult = amqpBasicConsumeTask.execute();
        assertThat(amqpBasicConsumeResult.status).isEqualTo(Status.Failure);

    }
}
