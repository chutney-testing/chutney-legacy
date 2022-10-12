package com.chutneytesting.action.amqp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.fail;

import com.chutneytesting.action.TestFinallyActionRegistry;
import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.ActionExecutionResult.Status;
import com.chutneytesting.action.spi.injectable.Target;
import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import com.google.common.collect.ImmutableList;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.LongString;
import com.rabbitmq.client.impl.LongStringHelper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import wiremock.com.google.common.collect.ImmutableMap;

@SuppressWarnings("unchecked")
public class AmqpActionsTest {

    @Test
    public void nominal_test() throws IOException {
        MockConnectionFactory mockConnectionFactory = new MockConnectionFactory();
        TestFinallyActionRegistry finallyActionRegistry = new TestFinallyActionRegistry();
        TestLogger logger = new TestLogger();
        Target target = TestTarget.TestTargetBuilder.builder()
            .withTargetId("rabbit")
            .withUrl("amqp://non_host:1234")
            .withProperty("user", "guest")
            .withProperty("password", "guest")
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
        Action amqpDeleteQueueAction = mockConnectionFactory(new AmqpDeleteQueueAction(
            target,
            queueName,
            logger
        ), mockConnectionFactory);

        assertThat(amqpDeleteQueueAction.execute().status).isEqualTo(Status.Success);
    }

    private void unbindQueue(MockConnectionFactory mockConnectionFactory, TestLogger logger, Target target, String queueName) {
        Action amqpUnbindQueueAction = mockConnectionFactory(new AmqpUnbindQueueAction(
            target,
            queueName,
            "test-ex",
            "routing.key",
            logger
        ), mockConnectionFactory);

        assertThat(amqpUnbindQueueAction.execute().status).isEqualTo(Status.Success);
    }

    private void basicGet(MockConnectionFactory mockConnectionFactory, TestLogger logger, Target target, String queueName) {
        Action amqpBasicGetAction = mockConnectionFactory(new AmqpBasicGetAction(
            target,
            queueName,
            logger
        ), mockConnectionFactory);

        Function<Object, Object> contentTypeExtractor = gr -> ((GetResponse) gr).getProps().getContentType();

        ActionExecutionResult amqpBasicGetResult = amqpBasicGetAction.execute();
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
        Action amqpBasicConsumeAction = mockConnectionFactory(new AmqpBasicConsumeAction(
            target,
            queueName,
            5,
            "",
            "10 sec",
            true,
            logger
        ), mockConnectionFactory);

        ActionExecutionResult amqpBasicConsumeResult = amqpBasicConsumeAction.execute();
        assertThat(amqpBasicConsumeResult.status).isEqualTo(Status.Success);
        assertThat(amqpBasicConsumeResult.outputs).hasSize(3);
        final List<Map<String, Object>> body = (List<Map<String, Object>>) amqpBasicConsumeResult.outputs.get("body");
        final List<Map<String, Object>> payloads = (List<Map<String, Object>>) amqpBasicConsumeResult.outputs.get("payloads");
        final List<Map<String, Object>> headers = (List<Map<String, Object>>) amqpBasicConsumeResult.outputs.get("headers");
        assertThat(body.size()).isEqualTo(5);
        final Map<String, Object> message = body.get(0);
        final Map<String, Object> payload1 = (Map<String, Object>) message.get("payload");
        assertThat(payload1.get("value")).isEqualTo("test message");
        final Map<String, Object> headers1 = (Map<String, Object>) message.get("headers");
        assertThat(headers1).containsAllEntriesOf(ImmutableMap.of("header1", "value1",
            "header2", ImmutableList.of("value1", "value2", "value3")));
        assertThat(payload1).isEqualTo(payloads.get(0));
        assertThat(headers1).isEqualTo(headers.get(0));
    }

    private void basicPublish(MockConnectionFactory mockConnectionFactory, TestLogger logger, Target target) {
        Action amqpBasicPublishAction = mockConnectionFactory(new AmqpBasicPublishAction(
            target,
            "test-ex",
            "test",
            Collections.singletonMap("header1", "value1"),
            Collections.singletonMap("content_type", "application/json"),
            "test message",
            logger
        ), mockConnectionFactory);

        assertThat(amqpBasicPublishAction.execute().status).isEqualTo(ActionExecutionResult.Status.Success);
    }

    private void basicPublishForConsuming(MockConnectionFactory mockConnectionFactory, TestLogger logger, Target target) {
        final LongString value1 = LongStringHelper.asLongString("value1".getBytes());
        final LongString value2 = LongStringHelper.asLongString("value2".getBytes());
        final String value3 = "value3";
        Action amqpBasicPublishAction = mockConnectionFactory(new AmqpBasicPublishAction(
            target,
            "test-ex",
            "test",
            ImmutableMap.of("header1", value1, "header2", ImmutableList.of(value1, value2, value3)),
            Collections.singletonMap("content_type", "application/json"),
            "{\"value\": \"test message\", \"id\": \"999999\"}",
            logger
        ), mockConnectionFactory);

        assertThat(amqpBasicPublishAction.execute().status).isEqualTo(ActionExecutionResult.Status.Success);
    }

    private String createTemporaryBoundQueue(MockConnectionFactory mockConnectionFactory, TestFinallyActionRegistry finallyActionRegistry, TestLogger logger, Target target, String queueName) {
        Action amqpCreateBoundTemporaryQueueAction = mockConnectionFactory(new AmqpCreateBoundTemporaryQueueAction(
            target,
            "test-ex",
            "routing.key",
            queueName,
            logger,
            finallyActionRegistry
        ), mockConnectionFactory);

        ActionExecutionResult amqpCreateBoundTemporaryQueueResult = amqpCreateBoundTemporaryQueueAction.execute();
        assertThat(amqpCreateBoundTemporaryQueueResult.status).isEqualTo(Status.Success);
        assertThat(finallyActionRegistry.finallyActions)
            .extracting(FinallyAction::type)
            .containsExactly("amqp-unbind-queue", "amqp-delete-queue");

        return (String) amqpCreateBoundTemporaryQueueResult.outputs.get("queueName");
    }

    private void cleanQueues(MockConnectionFactory mockConnectionFactory, TestLogger logger, Target target, List<String> queueName) {
        Action amqpCleanQueuesAction = mockConnectionFactory(new AmqpCleanQueuesAction(
            target,
            queueName,
            logger
        ), mockConnectionFactory);

        ActionExecutionResult amqpCleanQueueResult = amqpCleanQueuesAction.execute();
        assertThat(amqpCleanQueueResult.status).isEqualTo(Status.Success);
        assertThat(amqpCleanQueueResult.outputs).hasSize(0);
        assertThat(logger.info.get(logger.info.size() - 1)).isEqualTo("Purge queue " + queueName.get(1) + ". 3 messages deleted");
        assertThat(logger.info.get(logger.info.size() - 2)).isEqualTo("Purge queue " + queueName.get(0) + ". 2 messages deleted");
    }

    static <T extends Action> T mockConnectionFactory(T action, ConnectionFactory connectionFactory) {
        try {
            ConnectionFactoryFactory cff = new ConnectionFactoryFactory(connectionFactory);
            ReflectionTestUtils.setField(action, "connectionFactoryFactory", cff);
        } catch (Exception e) {
            fail(e.getMessage(), e);
        }
        return action;
    }
}
