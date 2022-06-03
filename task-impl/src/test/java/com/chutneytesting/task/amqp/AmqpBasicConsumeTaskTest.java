package com.chutneytesting.task.amqp;

import static com.chutneytesting.task.amqp.AmqpTasksTest.mockConnectionFactory;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.chutneytesting.task.TestFinallyActionRegistry;
import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.TestTarget;
import com.chutneytesting.task.amqp.consumer.ConsumerSupervisor;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.TaskExecutionResult.Status;
import com.chutneytesting.task.spi.injectable.Target;
import com.github.fridujo.rabbitmq.mock.MockChannel;
import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import wiremock.com.google.common.collect.ImmutableMap;

@SuppressWarnings("unchecked")
public class AmqpBasicConsumeTaskTest {

    private final Target target = TestTarget.TestTargetBuilder.builder()
        .withTargetId("rabbit")
        .withUrl("amqp://non_host:1234")
        .withProperty("user", "guest")
        .withProperty("password", "guest")
        .build();
    private TestLogger logger1;
    private TestLogger logger2;
    private final String firstQueueName = "queue1";
    private final String secondQueueName = "queue2";

    private MockConnectionFactory mockConnectionFactory;
    private MockChannel channel;

    private final String shortTimeout = "1 sec";
    private final String longTimeout = "3 sec";

    @BeforeEach
    public void setUpAmqpServer() throws IOException {
        logger1 = new TestLogger();
        logger2 = new TestLogger();

        mockConnectionFactory = new MockConnectionFactory();
        channel = mockConnectionFactory.newConnection().createChannel();
        channel.exchangeDeclare("test-ex", "fanout");

        Task amqpCreateBoundTemporaryQueueTask = mockConnectionFactory(new AmqpCreateBoundTemporaryQueueTask(
            target,
            "test-ex",
            "test",
            firstQueueName,
            logger1,
            new TestFinallyActionRegistry()
        ), mockConnectionFactory);

        TaskExecutionResult amqpCreateBoundTemporaryQueueResult = amqpCreateBoundTemporaryQueueTask.execute();
        assertThat(amqpCreateBoundTemporaryQueueResult.status).isEqualTo(Status.Success);

        Task amqpCreateBoundTemporaryQueueTask2 = mockConnectionFactory(new AmqpCreateBoundTemporaryQueueTask(
            target,
            "test-ex",
            "test2",
            secondQueueName,
            logger1,
            new TestFinallyActionRegistry()
        ), mockConnectionFactory);

        TaskExecutionResult amqpCreateBoundTemporaryQueueResult2 = amqpCreateBoundTemporaryQueueTask2.execute();
        assertThat(amqpCreateBoundTemporaryQueueResult2.status).isEqualTo(Status.Success);

        Task publishFirstMessage = mockConnectionFactory(new AmqpBasicPublishTask(
            target,
            "test-ex",
            "test",
            ImmutableMap.of("maVersion", "1.0", "timestamp", "987456321"),
            Collections.singletonMap("content_type", "application/json"),
            "{\"value\": \"test message\", \"id\": \"7777\"}",
            logger1
        ), mockConnectionFactory);
        assertThat(publishFirstMessage.execute().status).isEqualTo(TaskExecutionResult.Status.Success);
        assertThat(publishFirstMessage.execute().status).isEqualTo(TaskExecutionResult.Status.Success);

        Task publishSecondMessage = mockConnectionFactory(new AmqpBasicPublishTask(
            target,
            "test-ex",
            "test",
            ImmutableMap.of("maVersion", "2.0", "timestamp", "987456321"),
            Collections.singletonMap("content_type", "application/json"),
            "{\"value\": \"test message\", \"id\": \"1111\", \"item\": {\"id\": \"123\"}   }",
            logger1
        ), mockConnectionFactory);
        assertThat(publishSecondMessage.execute().status).isEqualTo(TaskExecutionResult.Status.Success);

        Task publishThirdMessageOnSecondQueue = mockConnectionFactory(new AmqpBasicPublishTask(
            target,
            "test-ex",
            "test2",
            ImmutableMap.of("maVersion", "10.0", "timestamp", "987456321"),
            Collections.singletonMap("content_type", "application/json"),
            "{\"value\": \"test message\", \"id\": \"1111\", \"item\": {\"id\": \"123\"}   }",
            logger1
        ), mockConnectionFactory);
        assertThat(publishThirdMessageOnSecondQueue.execute().status).isEqualTo(TaskExecutionResult.Status.Success);
    }

    @Test
    public void basicConsume_filter_messages_with_selector() {
        Task amqpBasicConsumeTask = mockConnectionFactory(new AmqpBasicConsumeTask(
            target,
            firstQueueName,
            1,
            "$..[?($.headers.maVersion=='2.0' && $.payload.item.id==\"123\")]",
            shortTimeout,
            true,
            logger1
        ), mockConnectionFactory);

        TaskExecutionResult amqpBasicConsumeResult = amqpBasicConsumeTask.execute();
        assertThat(amqpBasicConsumeResult.status).isEqualTo(Status.Success);
        assertThat(amqpBasicConsumeResult.outputs).hasSize(3);

        final List<Map<String, Object>> body = extractData(amqpBasicConsumeResult, "body");
        assertThat(body.size()).isEqualTo(1);

        final Map<String, Object> message = body.get(0);
        final Map<String, Object> payload1 = (Map<String, Object>) message.get("payload");
        assertThat(payload1.get("value")).isEqualTo("test message");
        assertThat(payload1.get("id")).isEqualTo("1111");

        final Map<String, Object> headers1 = (Map<String, Object>) message.get("headers");
        assertThat(headers1.get("maVersion")).isEqualTo("2.0");

        final List<Map<String, Object>> payloads = extractData(amqpBasicConsumeResult, "payloads");
        assertThat(payload1).isEqualTo(payloads.get(0));

        final List<Map<String, Object>> headers = extractData(amqpBasicConsumeResult, "headers");
        assertThat(headers1).isEqualTo(headers.get(0));

        // Assert that message matching selector is ack and the other is not
        channel.basicRecover();

        Task amqpBasicConsumeTaskRemainingMessage = mockConnectionFactory(new AmqpBasicConsumeTask(
            target,
            firstQueueName,
            1,
            "",
            shortTimeout,
            true,
            logger2
        ), mockConnectionFactory);

        TaskExecutionResult amqpBasicConsumeResultRemainingMessage = amqpBasicConsumeTaskRemainingMessage.execute();
        assertThat(amqpBasicConsumeResultRemainingMessage.status).isEqualTo(Status.Success);

        final List<Map<String, Object>> payloadsRemaining = extractData(amqpBasicConsumeResultRemainingMessage, "payloads");
        assertThat(payloadsRemaining.size()).isEqualTo(1);

        final List<Map<String, Object>> bodyRemaining = extractData(amqpBasicConsumeResultRemainingMessage, "body");
        final Map<String, Object> messageRemaining = bodyRemaining.get(0);
        final Map<String, Object> headersRemaining = (Map<String, Object>) messageRemaining.get("headers");
        assertThat(headersRemaining.get("maVersion")).isEqualTo("1.0");
    }

    @Test
    public void basicConsume_filter_messages_with_selector_return_not_expected_messages() {
        Task amqpBasicConsumeTask = mockConnectionFactory(new AmqpBasicConsumeTask(
            target,
            firstQueueName,
            1,
            "$..[?($.headers.maVersion=='nonExistingHeader')]",
            shortTimeout,
            true,
            logger1
        ), mockConnectionFactory);

        TaskExecutionResult amqpBasicConsumeResult = amqpBasicConsumeTask.execute();
        assertThat(amqpBasicConsumeResult.status).isEqualTo(Status.Failure);
    }

    @Test
    public void basicConsume_with_no_acknowledge_should_keep_messages_in_queue() {
        Task amqpBasicConsumeTask = mockConnectionFactory(new AmqpBasicConsumeTask(
            target,
            firstQueueName,
            1,
            "$..[?($.headers.maVersion=='1.0')]",
            longTimeout,
            false,
            logger1
        ), mockConnectionFactory);

        TaskExecutionResult amqpBasicConsumeResult = amqpBasicConsumeTask.execute();
        assertThat(amqpBasicConsumeResult.status).isEqualTo(Status.Success);

        channel.basicRecover();

        TaskExecutionResult amqpBasicConsumeResultAgain = amqpBasicConsumeTask.execute();
        assertThat(amqpBasicConsumeResultAgain.status).isEqualTo(Status.Success);
    }

    @Test
    public void should_have_only_one_consumer_consuming_messages_while_the_other_is_waiting() {
        Task shouldFailConsumer = mockConnectionFactory(new AmqpBasicConsumeTask(
            target,
            firstQueueName,
            1,
            "$..[?($.headers.maVersion=='nonExistingHeader')]",
            shortTimeout,
            false,
            logger1
        ), mockConnectionFactory);

        Task shouldSuccess = mockConnectionFactory(new AmqpBasicConsumeTask(
            target,
            firstQueueName,
            1,
            "$..[?($.headers.maVersion=='2.0')]",
            longTimeout,
            false,
            logger2
        ), mockConnectionFactory);


        AtomicReference<TaskExecutionResult> timeoutFailedResult = new AtomicReference<>();
        new Thread(() -> timeoutFailedResult.set(shouldFailConsumer.execute())).start();

        AtomicReference<TaskExecutionResult> result2 = new AtomicReference<>();
        await().atMost(1, SECONDS).untilAsserted(() -> {
                assertThat(ConsumerSupervisor.getInstance().isLocked(firstQueueName)).isTrue();
                new Thread(() -> result2.set(shouldSuccess.execute())).start();
            }
        );

        await().atMost(5, SECONDS).untilAsserted(() -> {
                assertThat(timeoutFailedResult.get()).isNotNull();
                assertThat(result2.get()).isNotNull();
                assertThat(timeoutFailedResult.get().status).isEqualTo(Status.Failure);
                assertThat(logger1.errors).anyMatch(s -> s.contains("Unable to get the expected number of messages [1] during 1 sec"));
                assertThat(result2.get().status).isEqualTo(Status.Success);
                assertThat(logger2.info).anyMatch(s -> s.contains("ms to acquire lock to consume queue " + firstQueueName));
                assertThat(ConsumerSupervisor.getInstance().isLocked(firstQueueName)).isFalse();
            }
        );
    }

    @Test
    public void should_not_consume_message_when_another_one_is_consuming() {
        Task lockQueueAndTimeoutConsumer = mockConnectionFactory(new AmqpBasicConsumeTask(
            target,
            firstQueueName,
            1,
            "$..[?($.headers.maVersion=='nonExistingHeader')]",
            shortTimeout,
            false,
            logger1
        ), mockConnectionFactory);

        Task shouldSuccess = mockConnectionFactory(new AmqpBasicConsumeTask(
            target,
            firstQueueName,
            1,
            "$..[?($.headers.maVersion=='2.0')]",
            shortTimeout,
            false,
            logger2
        ), mockConnectionFactory);


        AtomicReference<TaskExecutionResult> timeoutFailedResult = new AtomicReference<>();
        new Thread(() -> timeoutFailedResult.set(lockQueueAndTimeoutConsumer.execute())).start();

        AtomicReference<TaskExecutionResult> result2 = new AtomicReference<>();
        await().atMost(1, SECONDS).untilAsserted(() ->
            {
                assertThat(ConsumerSupervisor.getInstance().isLocked(firstQueueName)).isTrue();
                new Thread(() -> result2.set(shouldSuccess.execute())).start();
            }
        );

        await().atMost(5, SECONDS).untilAsserted(() -> {
                assertThat(timeoutFailedResult.get()).isNotNull();
                assertThat(result2.get()).isNotNull();
                assertThat(timeoutFailedResult.get().status).isEqualTo(Status.Failure);
                assertThat(result2.get().status).isEqualTo(Status.Failure);
                assertThat(logger2.errors).containsAnyOf("Cannot consume on queue [" + firstQueueName + "]. Another consumer already listening on this queue");
                assertThat(ConsumerSupervisor.getInstance().isLocked(firstQueueName)).isFalse();
            }
        );
    }

    @Test
    public void should_consume_message_one_another_queue() {
        Task shouldFailConsumer = mockConnectionFactory(new AmqpBasicConsumeTask(
            target,
            firstQueueName,
            1,
            "$..[?($.headers.maVersion=='nonExistingHeader')]",
            shortTimeout,
            false,
            logger1
        ), mockConnectionFactory);

        Task shouldSuccess = mockConnectionFactory(new AmqpBasicConsumeTask(
            target,
            secondQueueName,
            1,
            "$..[?($.headers.maVersion=='10.0')]",
            shortTimeout,
            false,
            logger2
        ), mockConnectionFactory);


        AtomicReference<TaskExecutionResult> result1 = new AtomicReference<>();
        new Thread(() -> result1.set(shouldFailConsumer.execute())).start();

        AtomicReference<TaskExecutionResult> result2 = new AtomicReference<>();
        await().atMost(1, SECONDS).untilAsserted(() -> {
                assertThat(ConsumerSupervisor.getInstance().isLocked(firstQueueName)).isTrue();
                new Thread(() -> result2.set(shouldSuccess.execute())).start();
            }
        );

        await().atMost(1, SECONDS).untilAsserted(() ->
            assertThat(ConsumerSupervisor.getInstance().isLocked(secondQueueName)).isTrue()
        );

        await().atMost(5, SECONDS).untilAsserted(() -> {
                assertThat(result1.get()).isNotNull();
                assertThat(result2.get()).isNotNull();
                assertThat(result1.get().status).isEqualTo(Status.Failure);
                assertThat(result2.get().status).isEqualTo(Status.Success);
                assertThat(logger1.errors).contains("Unable to get the expected number of messages [1] during 1 sec.");
                assertThat(ConsumerSupervisor.getInstance().isLocked(firstQueueName)).isFalse();
                assertThat(ConsumerSupervisor.getInstance().isLocked(secondQueueName)).isFalse();
            }
        );
    }

    private List<Map<String, Object>> extractData(TaskExecutionResult amqpBasicConsumeResult, String dataName) {
        return (List<Map<String, Object>>) amqpBasicConsumeResult.outputs.get(dataName);
    }
}
