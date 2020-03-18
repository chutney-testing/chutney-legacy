package com.chutneytesting.task.kafka;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Failure;
import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.TestTarget;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Target;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.Test;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.test.util.ReflectionTestUtils;
import wiremock.com.google.common.collect.ImmutableMap;

public class KafkaBasicConsumeTaskTest {

    private static final String TOPIC = "topic";
    public static final String GROUP = "mygroup";
    private static final long TIMESTAMP = 42L;
    private static final TimestampType TIMESTAMP_TYPE = TimestampType.CREATE_TIME;
    private static final long FIRST_OFFSET = 0L;
    private static final int PARTITION = 0;


    private Target getKafkaTarget() {
        return TestTarget.TestTargetBuilder.builder()
            .withTargetId("kafka")
            .withUrl("tcp://127.0.0.1:5555")
            .build();
    }

    @Test
    public void basic_consume_task_should_success() throws Exception {
        //given
        TestLogger logger = new TestLogger();
        Task task = new KafkaBasicConsumeTask(getKafkaTarget(), TOPIC, GROUP, Collections.emptyMap(), 1, null, "10 sec", logger);
        ConsumerFactory<String, String> cf = mock(ConsumerFactory.class);
        Consumer<String, String> consumer = mock(Consumer.class);
        given(cf.createConsumer(any(), any(), any(), any())).willReturn(consumer);
        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        containerProperties.setGroupId(GROUP);
        containerProperties.setMessageListener(ReflectionTestUtils.invokeMethod(task, "createMessageListener"));
        ConcurrentMessageListenerContainer<String, String> messageListenerContainer = new ConcurrentMessageListenerContainer<>(cf, containerProperties);
        ReflectionTestUtils.setField(task, "messageListenerContainer", messageListenerContainer);
        MessageListener<String, String> listener = (MessageListener<String, String>) messageListenerContainer.getContainerProperties().getMessageListener();
        RecordHeaders recordHeaders = getHeaders();
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(TOPIC, PARTITION, FIRST_OFFSET, TIMESTAMP, TIMESTAMP_TYPE, 0L, 0, 0, "KEY", "{\"value\": \"test message\", \"id\": \"1111\" }", recordHeaders);
        listener.onMessage(consumerRecord);

        //when
        TaskExecutionResult taskExecutionResult = task.execute();

        //Then
        assertThat(taskExecutionResult.status).isEqualTo(Success);
        assertThat(taskExecutionResult.outputs).hasSize(3);
        final List<Map<String, Object>> body = (List<Map<String, Object>>) taskExecutionResult.outputs.get("body");
        final List<Map<String, Object>> payloads = (List<Map<String, Object>>) taskExecutionResult.outputs.get("payloads");
        final List<Map<String, Object>> headers = (List<Map<String, Object>>) taskExecutionResult.outputs.get("headers");
        final Map<String, Object> message = body.get(0);
        final Map<String, Object> payload1 = (Map<String, Object>) message.get("payload");
        final Map<String, Object> headers1 = (Map<String, Object>) message.get("headers");
        assertThat(body.size()).isEqualTo(1);
        assertThat(payload1.get("value")).isEqualTo("test message");
        assertThat(payload1.get("id")).isEqualTo("1111");
        assertThat(headers1.get("X-Custom-HeaderKey")).isEqualTo("X-Custom-HeaderValue");
        assertThat(headers1).containsAllEntriesOf(ImmutableMap.of("X-Custom-HeaderKey", "X-Custom-HeaderValue", "header1", "value1"));
        assertThat(payload1).isEqualTo(payloads.get(0));
        assertThat(headers1).isEqualTo(headers.get(0));
        assertThat(logger.errors).isEmpty();
    }

    private RecordHeaders getHeaders() {
        List<Header> headersList = ImmutableList.of(new RecordHeader("X-Custom-HeaderKey", "X-Custom-HeaderValue".getBytes()), new RecordHeader("header1", "value1".getBytes()));
        return new RecordHeaders(headersList);
    }

    @Test
    public void basic_consume_task_should_success_with_selector() throws Exception {
        //given
        TestLogger logger = new TestLogger();
        Task task = new KafkaBasicConsumeTask(getKafkaTarget(), TOPIC, GROUP, Collections.emptyMap(), 1, "$..[?($.headers.header1=='value1' && $.payload.id==\"1122\")]", "10 sec", logger);
        ConsumerFactory<String, String> cf = mock(ConsumerFactory.class);
        Consumer<String, String> consumer = mock(Consumer.class);
        given(cf.createConsumer(any(), any(), any(), any())).willReturn(consumer);
        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        containerProperties.setGroupId(GROUP);
        containerProperties.setMessageListener(ReflectionTestUtils.invokeMethod(task, "createMessageListener"));
        ConcurrentMessageListenerContainer<String, String> messageListenerContainer = new ConcurrentMessageListenerContainer<>(cf, containerProperties);
        ReflectionTestUtils.setField(task, "messageListenerContainer", messageListenerContainer);
        MessageListener<String, String> listener = (MessageListener<String, String>) messageListenerContainer.getContainerProperties().getMessageListener();
        RecordHeaders recordHeaders = getHeaders();
        ConsumerRecord<String, String> consumerRecord1 = new ConsumerRecord<>(TOPIC, PARTITION, FIRST_OFFSET, TIMESTAMP, TIMESTAMP_TYPE, 0L, 0, 0, "KEY1", "{\"value\": \"test message1\", \"id\": \"1111\" }", recordHeaders);
        ConsumerRecord<String, String> consumerRecord2 = new ConsumerRecord<>(TOPIC, PARTITION, FIRST_OFFSET + 1, TIMESTAMP, TIMESTAMP_TYPE, 0L, 0, 0, "KEY2", "{\"value\": \"test message2\", \"id\": \"1122\" }", recordHeaders);
        ConsumerRecord<String, String> consumerRecord3 = new ConsumerRecord<>(TOPIC, PARTITION, FIRST_OFFSET + 2, TIMESTAMP, TIMESTAMP_TYPE, 0L, 0, 0, "KEY2", "{\"value\": \"test message3\", \"id\": \"1133\" }", recordHeaders);
        listener.onMessage(consumerRecord1);
        listener.onMessage(consumerRecord2);
        listener.onMessage(consumerRecord3);

        //when
        TaskExecutionResult taskExecutionResult = task.execute();

        //Then
        assertThat(taskExecutionResult.status).isEqualTo(Success);
        final List<Map<String, Object>> body = (List<Map<String, Object>>) taskExecutionResult.outputs.get("body");
        final Map<String, Object> message = body.get(0);
        final Map<String, Object> payload1 = (Map<String, Object>) message.get("payload");
        final Map<String, Object> headers1 = (Map<String, Object>) message.get("headers");
        assertThat(body.size()).isEqualTo(1);
        assertThat(payload1.get("value")).isEqualTo("test message2");
        assertThat(payload1.get("id")).isEqualTo("1122");
        assertThat(headers1.get("X-Custom-HeaderKey")).isEqualTo("X-Custom-HeaderValue");
        assertThat(logger.errors).isEmpty();

    }

    @Test
    public void basic_consume_task_should_failed_when_timeout() throws Exception {
        //given
        TestLogger logger = new TestLogger();
        Task task = new KafkaBasicConsumeTask(getKafkaTarget(), TOPIC, "mygroup", Collections.emptyMap(), 1, null, "10 sec", logger);
        ConsumerFactory<String, String> cf = mock(ConsumerFactory.class);
        Consumer<String, String> consumer = mock(Consumer.class);
        given(cf.createConsumer(any(), any(), any(), any())).willReturn(consumer);
        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        containerProperties.setGroupId(GROUP);
        containerProperties.setMessageListener(ReflectionTestUtils.invokeMethod(task, "createMessageListener"));
        ConcurrentMessageListenerContainer<String, String> messageListenerContainer = new ConcurrentMessageListenerContainer<>(cf, containerProperties);
        ReflectionTestUtils.setField(task, "messageListenerContainer", messageListenerContainer);

        //when
        TaskExecutionResult taskExecutionResult = task.execute();

        //Then
        assertThat(taskExecutionResult.status).isEqualTo(Failure);
        assertThat(logger.errors).isNotEmpty();

    }

}
