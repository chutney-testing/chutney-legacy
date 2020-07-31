package com.chutneytesting.task.amqp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import com.google.common.collect.ImmutableList;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.LongString;
import com.rabbitmq.client.impl.LongStringHelper;
import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.TaskExecutionResult.Status;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.TestFinallyActionRegistry;
import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.TestTarget;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import wiremock.com.google.common.collect.ImmutableMap;

public class AmqpTasksTest {

    @Test
    public void nominal_test() throws IOException {
        MockConnectionFactory mockConnectionFactory = new MockConnectionFactory();
        TestFinallyActionRegistry finallyActionRegistry = new TestFinallyActionRegistry();
        TestLogger logger = new TestLogger();
        Target target = TestTarget.TestTargetBuilder.builder()
            .withTargetId("rabbit")
            .withUrl("amqp://non_host:1234")
            .withSecurity("guest", "guest")
            .build();

        mockConnectionFactory.newConnection().createChannel().exchangeDeclare("test-ex", "fanout");

        String queueName = createTemporaryBoundQueue(mockConnectionFactory, finallyActionRegistry, logger, target, "queueName");

        String otherQueueName = createTemporaryBoundQueue(mockConnectionFactory, new TestFinallyActionRegistry(), logger, target, "otherQueueName");

        basicPublish(mockConnectionFactory, logger, target);

        basicGet(mockConnectionFactory, logger, target, queueName);

        basicPublish(mockConnectionFactory, logger, target);

        basicPublish(mockConnectionFactory, logger, target);

        cleanQueues(mockConnectionFactory, logger, target, Arrays.asList(queueName, otherQueueName));

        basicPublishForConsuming(mockConnectionFactory, logger, target);
        basicPublishForConsuming(mockConnectionFactory, logger, target);
        basicPublishForConsuming(mockConnectionFactory, logger, target);
        basicPublishForConsuming(mockConnectionFactory, logger, target);
        basicPublishForConsuming(mockConnectionFactory, logger, target);

        basicConsume(mockConnectionFactory, logger, target, queueName);

        unbindQueue(mockConnectionFactory, logger, target, queueName);

        deleteQueue(mockConnectionFactory, logger, target, queueName);
    }

    private void deleteQueue(MockConnectionFactory mockConnectionFactory, TestLogger logger, Target target, String queueName) {
        Task amqpDeleteQueueTask = mockConnectionFactory(new AmqpDeleteQueueTask(
            target,
            queueName,
            logger
        ), mockConnectionFactory);

        assertThat(amqpDeleteQueueTask.execute().status).isEqualTo(Status.Success);
    }

    private void unbindQueue(MockConnectionFactory mockConnectionFactory, TestLogger logger, Target target, String queueName) {
        Task amqpUnbindQueueTask = mockConnectionFactory(new AmqpUnbindQueueTask(
            target,
            queueName,
            "test-ex",
            "routing.key",
            logger
        ), mockConnectionFactory);

        assertThat(amqpUnbindQueueTask.execute().status).isEqualTo(Status.Success);
    }

    private void basicGet(MockConnectionFactory mockConnectionFactory, TestLogger logger, Target target, String queueName) {
        Task amqpBasicGetTask = mockConnectionFactory(new AmqpBasicGetTask(
            target,
            queueName,
            logger
        ), mockConnectionFactory);

        Function<Object, Object> contentTypeExtractor = gr -> ((GetResponse) gr).getProps().getContentType();

        TaskExecutionResult amqpBasicGetResult = amqpBasicGetTask.execute();
        assertThat(amqpBasicGetResult.status).isEqualTo(Status.Success);
        assertThat(amqpBasicGetResult.outputs).hasSize(3);
        assertThat(amqpBasicGetResult.outputs.get("message"))
            .isExactlyInstanceOf(GetResponse.class)
            .extracting(contentTypeExtractor)
            .isEqualTo("application/json");
        assertThat(amqpBasicGetResult.outputs.get("body")).isEqualTo("test message");
        assertThat((Map<String, Object>) amqpBasicGetResult.outputs.get("headers")).containsOnly(entry("header1", "value1"));
    }

    private void basicConsume(MockConnectionFactory mockConnectionFactory, TestLogger logger, Target target, String queueName) {
        Task amqpBasicConsumeTask = mockConnectionFactory(new AmqpBasicConsumeTask(
            target,
            queueName,
            5,
            "",
            "10 sec",
            true,
            logger
        ), mockConnectionFactory);

        TaskExecutionResult amqpBasicConsumeResult = amqpBasicConsumeTask.execute();
        assertThat(amqpBasicConsumeResult.status).isEqualTo(Status.Success);
        assertThat(amqpBasicConsumeResult.outputs).hasSize(3);
        final List<Map<String, Object>> body = (List<Map<String, Object>>) amqpBasicConsumeResult.outputs.get("body");
        final List<Map<String, Object>> payloads = (List<Map<String, Object>>) amqpBasicConsumeResult.outputs.get("payloads");
        final List<Map<String, Object>> headers = (List<Map<String, Object>>) amqpBasicConsumeResult.outputs.get("headers");
        assertThat(body.size()).isEqualTo(5);
        final Map<String, Object> message = body.get(0);
        final Map<String, Object>  payload1 = (Map<String, Object>) message.get("payload");
        assertThat(payload1.get("value")).isEqualTo("test message");
        final Map<String, Object> headers1 = (Map<String, Object>) message.get("headers");
        assertThat(headers1).containsAllEntriesOf(ImmutableMap.of("header1", "value1",
            "header2", ImmutableList.of("value1", "value2", "value3")));
        assertThat(payload1).isEqualTo(payloads.get(0));
        assertThat(headers1).isEqualTo(headers.get(0));
    }

    private void basicPublish(MockConnectionFactory mockConnectionFactory, TestLogger logger, Target target) {
        Task amqpBasicPublishTask = mockConnectionFactory(new AmqpBasicPublishTask(
            target,
            "test-ex",
            "test",
            Collections.singletonMap("header1", "value1"),
            Collections.singletonMap("content_type", "application/json"),
            "test message",
            logger
        ), mockConnectionFactory);

        assertThat(amqpBasicPublishTask.execute().status).isEqualTo(TaskExecutionResult.Status.Success);
    }

    private void basicPublishForConsuming(MockConnectionFactory mockConnectionFactory, TestLogger logger, Target target) {
        final LongString value1 = LongStringHelper.asLongString("value1".getBytes());
        final LongString value2 = LongStringHelper.asLongString("value2".getBytes());
        final String value3 = "value3";
        Task amqpBasicPublishTask = mockConnectionFactory(new AmqpBasicPublishTask(
            target,
            "test-ex",
            "test",
            ImmutableMap.of("header1", value1, "header2", ImmutableList.of(value1, value2, value3)),
            Collections.singletonMap("content_type", "application/json"),
            "{\"value\": \"test message\", \"id\": \"999999\"}",
            logger
        ), mockConnectionFactory);

        assertThat(amqpBasicPublishTask.execute().status).isEqualTo(TaskExecutionResult.Status.Success);
    }

    private String createTemporaryBoundQueue(MockConnectionFactory mockConnectionFactory, TestFinallyActionRegistry finallyActionRegistry, TestLogger logger, Target target, String queueName) {
        Task amqpCreateBoundTemporaryQueueTask = mockConnectionFactory(new AmqpCreateBoundTemporaryQueueTask(
            target,
            "test-ex",
            "routing.key",
            queueName,
            logger,
            finallyActionRegistry
        ), mockConnectionFactory);

        TaskExecutionResult amqpCreateBoundTemporaryQueueResult = amqpCreateBoundTemporaryQueueTask.execute();
        assertThat(amqpCreateBoundTemporaryQueueResult.status).isEqualTo(Status.Success);
        assertThat(finallyActionRegistry.finallyActions)
            .extracting(FinallyAction::actionIdentifier)
            .containsExactly("amqp-delete-queue", "amqp-unbind-queue");

        return (String) amqpCreateBoundTemporaryQueueResult.outputs.get("queueName");
    }

    private void cleanQueues(MockConnectionFactory mockConnectionFactory, TestLogger logger, Target target, List<String> queueName) {
        Task amqpCleanQueuesTask = mockConnectionFactory(new AmqpCleanQueuesTask(
            target,
            queueName,
            logger
        ), mockConnectionFactory);


        TaskExecutionResult amqpCleanQueueResult = amqpCleanQueuesTask.execute();
        assertThat(amqpCleanQueueResult.status).isEqualTo(Status.Success);
        assertThat(amqpCleanQueueResult.outputs).hasSize(0);
        assertThat(logger.info.get(logger.info.size() - 1)).isEqualTo("Purge queue " + queueName.get(1) + ". 3 messages deleted");
        assertThat(logger.info.get(logger.info.size() - 2)).isEqualTo("Purge queue " + queueName.get(0) + ". 2 messages deleted");
    }


    static <T extends Task> T mockConnectionFactory(T task, ConnectionFactory connectionFactory) {
        ReflectionTestUtils.setField(task, "connectionFactory", connectionFactory);
        return task;
    }
}
