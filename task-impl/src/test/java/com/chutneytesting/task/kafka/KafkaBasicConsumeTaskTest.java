package com.chutneytesting.task.kafka;

import static com.chutneytesting.task.kafka.KafkaBasicConsumeTask.OUTPUT_BODY;
import static com.chutneytesting.task.kafka.KafkaBasicConsumeTask.OUTPUT_BODY_HEADERS_KEY;
import static com.chutneytesting.task.kafka.KafkaBasicConsumeTask.OUTPUT_BODY_PAYLOAD_KEY;
import static com.chutneytesting.task.kafka.KafkaBasicConsumeTask.OUTPUT_HEADERS;
import static com.chutneytesting.task.kafka.KafkaBasicConsumeTask.OUTPUT_PAYLOADS;
import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Failure;
import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_XML_VALUE;
import static org.springframework.util.MimeTypeUtils.TEXT_PLAIN_VALUE;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.TestTarget;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Target;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.test.util.ReflectionTestUtils;
import wiremock.com.google.common.collect.ImmutableMap;

@SuppressWarnings("unchecked")
@RunWith(JUnitParamsRunner.class)
public class KafkaBasicConsumeTaskTest {

    private static final String TOPIC = "topic";
    private static final String GROUP = "mygroup";
    private static final long TIMESTAMP = 42L;
    private static final TimestampType TIMESTAMP_TYPE = TimestampType.CREATE_TIME;
    private static final long FIRST_OFFSET = 0L;
    private static final int PARTITION = 0;

    private static final Target TARGET_STUB = TestTarget.TestTargetBuilder.builder()
        .withTargetId("kafka")
        .withUrl("tcp://127.0.0.1:5555")
        .build();

    private TestLogger logger;

    @Before
    public void before() {
        logger = new TestLogger();
    }

    @Test
    public void should_consume_simple_text_message() {
        // Given
        Task sut = givenKafkaConsumeTask(null, TEXT_PLAIN_VALUE, null);
        givenTaskReceiveMessages(sut,
            buildRecord(FIRST_OFFSET, "KEY", "test message")
        );

        // When
        TaskExecutionResult taskExecutionResult = sut.execute();

        // Then
        assertThat(taskExecutionResult.status).isEqualTo(Success);
        List<Map<String, Object>> body = assertTaskOutputsSize(taskExecutionResult, 1);

        final Map<String, Object> message = body.get(0);
        final String payload1 = (String) message.get(OUTPUT_BODY_PAYLOAD_KEY);
        assertThat(payload1).isEqualTo("test message");
        final Map<String, Object> headers = (Map<String, Object>) message.get(OUTPUT_BODY_HEADERS_KEY);
        assertThat(headers.get("X-Custom-HeaderKey")).isEqualTo("X-Custom-HeaderValue");
        assertThat(headers).containsAllEntriesOf(ImmutableMap.of("X-Custom-HeaderKey", "X-Custom-HeaderValue", "header1", "value1"));

        assertThat(logger.errors).isEmpty();
    }

    @Test
    @Parameters({APPLICATION_JSON_VALUE, "null"})
    public void should_consume_json_message_as_map(@Nullable String mimeType) {
        // Given
        Task task = givenKafkaConsumeTask(null, mimeType, null);
        givenTaskReceiveMessages(task,
            buildRecord(FIRST_OFFSET, "KEY", "{\"value\": \"test message\", \"id\": \"1111\" }")
        );

        // When
        TaskExecutionResult taskExecutionResult = task.execute();

        // Then
        assertThat(taskExecutionResult.status).isEqualTo(Success);

        final Map<String, Object> payload = ((List<Map<String, Object>>) taskExecutionResult.outputs.get(OUTPUT_PAYLOADS)).get(0);
        assertThat(payload.get("value")).isEqualTo("test message");
        assertThat(payload.get("id")).isEqualTo("1111");
    }

    @Test
    public void should_consume_xml_message_as_string() {
        // Given
        Task task = givenKafkaConsumeTask(null, APPLICATION_XML_VALUE, null);
        String xmlPayload = "<root><first>first content</first><second attr=\"second attr\">second content</second></root>";
        givenTaskReceiveMessages(task,
            buildRecord(FIRST_OFFSET, "KEY", xmlPayload)
        );

        // When
        TaskExecutionResult taskExecutionResult = task.execute();

        // Then
        assertThat(taskExecutionResult.status).isEqualTo(Success);

        final String payload = ((List<String>) taskExecutionResult.outputs.get(OUTPUT_PAYLOADS)).get(0);
        assertThat(payload).isEqualTo(xmlPayload);
    }

    @Test
    public void should_select_json_message_whose_payload_or_headers_match_given_payload_jsonpath_selector() {
        // Given
        Task task = givenKafkaConsumeTask("$..[?($.headers.header1=='value1' && $.payload.id==\"1122\")]", APPLICATION_JSON_VALUE, null);
        givenTaskReceiveMessages(task,
            buildRecord(FIRST_OFFSET, "KEY1", "{\"value\": \"test message1\", \"id\": \"1111\" }"),
            buildRecord(FIRST_OFFSET + 1, "KEY2", "{\"value\": \"test message2\", \"id\": \"1122\" }"),
            buildRecord(FIRST_OFFSET + 2, "KEY3", "{\"value\": \"test message3\", \"id\": \"1133\" }")
        );

        // When
        TaskExecutionResult taskExecutionResult = task.execute();

        // Then
        assertThat(taskExecutionResult.status).isEqualTo(Success);

        List<Map<String, Object>> body = (List<Map<String, Object>>) taskExecutionResult.outputs.get(OUTPUT_BODY);
        assertThat(body).hasSize(1);
        final Map<String, Object> payload = ((List<Map<String, Object>>) taskExecutionResult.outputs.get(OUTPUT_PAYLOADS)).get(0);
        assertThat(payload.get("id")).isEqualTo("1122");
    }

    @Test
    public void should_select_xml_message_whose_payload_match_given_payload_xpath_selector() {
        // Given
        Task task = givenKafkaConsumeTask("/root/second[@attr='1122']", APPLICATION_XML_VALUE, null);
        String payloadToSelect = "<root><first>first content</first><second attr=\"1122\">second content</second></root>";
        givenTaskReceiveMessages(task,
            buildRecord(FIRST_OFFSET, "KEY1", "<root><first>first content</first><second attr=\"1111\">second content</second></root>"),
            buildRecord(FIRST_OFFSET + 1, "KEY2", payloadToSelect),
            buildRecord(FIRST_OFFSET + 2, "KEY3", "<root><first>first content</first><second attr=\"1133\">second content</second></root>")
        );

        // When
        TaskExecutionResult taskExecutionResult = task.execute();

        // Then
        assertThat(taskExecutionResult.status).isEqualTo(Success);

        List<Map<String, Object>> body = (List<Map<String, Object>>) taskExecutionResult.outputs.get(OUTPUT_BODY);
        assertThat(body).hasSize(1);
        final String payload = ((List<String>) taskExecutionResult.outputs.get(OUTPUT_PAYLOADS)).get(0);
        assertThat(payload).isEqualTo(payloadToSelect);
    }

    @Test
    public void should_select_text_message_whose_payload_contains_given_payload_selector() {
        // Given
        Task task = givenKafkaConsumeTask("selector", TEXT_PLAIN_VALUE, null);
        String payloadToSelect = "second text selector message";
        givenTaskReceiveMessages(task,
            buildRecord(FIRST_OFFSET, "KEY1", "first text message"),
            buildRecord(FIRST_OFFSET + 1, "KEY2", payloadToSelect),
            buildRecord(FIRST_OFFSET + 2, "KEY3", "third text message")
        );

        // When
        TaskExecutionResult taskExecutionResult = task.execute();

        // Then
        assertThat(taskExecutionResult.status).isEqualTo(Success);

        List<Map<String, Object>> body = (List<Map<String, Object>>) taskExecutionResult.outputs.get(OUTPUT_BODY);
        assertThat(body).hasSize(1);
        final String payload = ((List<String>) taskExecutionResult.outputs.get(OUTPUT_PAYLOADS)).get(0);
        assertThat(payload).isEqualTo(payloadToSelect);
    }

    @Test
    public void should_select_message_whose_headers_match_given_payload_jsonpath_selector() {
        // Given
        ImmutableList<Header> headers = ImmutableList.of(
            new RecordHeader("X-Custom-HeaderKey", "X-Custom-HeaderValue".getBytes()),
            new RecordHeader("header", "666".getBytes())
        );
        Task task = givenKafkaConsumeTask(3, null, "$..[?($.header=='666')]", null, null);
        String textMessageToSelect = "first text message";
        String xmlMessageToSelect = "<root>first xml message</root>";
        String jsonMessageToSelect = "first json message";
        givenTaskReceiveMessages(task,
            buildRecord(FIRST_OFFSET, "KEY1", textMessageToSelect, headers),
            buildRecord(FIRST_OFFSET + 1, "KEY2", "second text message"),
            buildRecord(FIRST_OFFSET + 2, "KEY3", xmlMessageToSelect, headers),
            buildRecord(FIRST_OFFSET + 3, "KEY4", "<root>second xml message</root>"),
            buildRecord(FIRST_OFFSET + 4, "KEY5", "{\"value\": \"" + jsonMessageToSelect + "\", \"id\": \"1\" }", headers),
            buildRecord(FIRST_OFFSET + 5, "KEY6", "{\"value\": \"second json message\", \"id\": \"1\" }")
        );

        // When
        TaskExecutionResult taskExecutionResult = task.execute();

        // Then
        assertThat(taskExecutionResult.status).isEqualTo(Success);
        List<Map<String, Object>> body = assertTaskOutputsSize(taskExecutionResult, 3);

        final String payload = (String) body.get(0).get(OUTPUT_BODY_PAYLOAD_KEY);
        assertThat(payload).isEqualTo(textMessageToSelect);
        final String xmlPayload = (String) body.get(1).get(OUTPUT_BODY_PAYLOAD_KEY);
        assertThat(xmlPayload).isEqualTo(xmlMessageToSelect);
        final Map jsonPayload = (Map) body.get(2).get(OUTPUT_BODY_PAYLOAD_KEY);
        assertThat(jsonPayload.get("value")).isEqualTo(jsonMessageToSelect);
    }

    @Test
    @Parameters({"Content-Type", "Contenttype", "content type"})
    public void should_override_given_mime_type_by_message_header(String contentTypeHeaderKey) {
        // Given
        ImmutableList<Header> headers = ImmutableList.of(
            new RecordHeader("X-Custom-HeaderKey", "X-Custom-HeaderValue".getBytes()),
            new RecordHeader(contentTypeHeaderKey, APPLICATION_JSON_VALUE.getBytes())
        );
        Task task = givenKafkaConsumeTask(null, APPLICATION_XML_VALUE, null);
        givenTaskReceiveMessages(task,
            buildRecord(FIRST_OFFSET, "KEY1", "{\"value\": \"test message\", \"id\": \"1\" }", headers)
        );

        // When
        TaskExecutionResult taskExecutionResult = task.execute();

        // Then
        assertThat(taskExecutionResult.status).isEqualTo(Success);
        List<Map<String, Object>> body = assertTaskOutputsSize(taskExecutionResult, 1);

        assertThat(logger.info).contains("Found content type header " + APPLICATION_JSON_VALUE);

        final Map payload = (Map) body.get(0).get(OUTPUT_BODY_PAYLOAD_KEY);
        assertThat(payload)
            .containsEntry("value", "test message")
            .containsEntry("id", "1");
    }

    @Test
    public void should_respect_given_timeout() {
        // Given
        Task task = givenKafkaConsumeTask(null, null, "3 sec");
        overrideTaskMessageListenerContainer(task);

        // When
        TaskExecutionResult taskExecutionResult = task.execute();

        // Then
        assertThat(taskExecutionResult.status).isEqualTo(Failure);
        assertThat(logger.errors).isNotEmpty();
    }

    @Test
    @Parameters({"bad content type", APPLICATION_JSON_VALUE, "\"" + APPLICATION_JSON_VALUE + "\""})
    public void should_consume_as_json_with_bad_content_type_in_received_message(String contentType) {
        ImmutableList<Header> headers = ImmutableList.of(
            new RecordHeader("Content-type", contentType.getBytes())
        );
        // Given
        Task task = givenKafkaConsumeTask(null, null, null);
        givenTaskReceiveMessages(task,
            buildRecord(FIRST_OFFSET, "KEY", "{\"value\": \"test message\", \"id\": \"1111\" }", headers)
        );

        // When
        TaskExecutionResult taskExecutionResult = task.execute();

        // Then
        assertThat(taskExecutionResult.status).isEqualTo(Success);

        final Map<String, Object> payload = ((List<Map<String, Object>>) taskExecutionResult.outputs.get(OUTPUT_PAYLOADS)).get(0);
        assertThat(payload.get("value")).isEqualTo("test message");
        assertThat(payload.get("id")).isEqualTo("1111");
    }

    private MessageListener<String, String> overrideTaskMessageListenerContainer(Task task) {
        ConsumerFactory<String, String> cf = mock(ConsumerFactory.class);
        Consumer<String, String> consumer = mock(Consumer.class);
        given(cf.createConsumer(any(), any(), any(), any())).willReturn(consumer);
        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        containerProperties.setGroupId(GROUP);
        containerProperties.setMessageListener(ReflectionTestUtils.invokeMethod(task, "createMessageListener"));
        ConcurrentMessageListenerContainer<String, String> messageListenerContainer = new ConcurrentMessageListenerContainer<>(cf, containerProperties);
        ReflectionTestUtils.setField(task, "messageListenerContainer", messageListenerContainer);
        return (MessageListener<String, String>) messageListenerContainer.getContainerProperties().getMessageListener();
    }

    private ConsumerRecord<String, String> buildRecord(long offset, String key, String payload) {
        List<Header> headersList = ImmutableList.of(new RecordHeader("X-Custom-HeaderKey", "X-Custom-HeaderValue".getBytes()), new RecordHeader("header1", "value1".getBytes()));
        return new ConsumerRecord<>(TOPIC, PARTITION, offset, TIMESTAMP, TIMESTAMP_TYPE, 0L, 0, 0, key, payload, new RecordHeaders(headersList));
    }

    private ConsumerRecord<String, String> buildRecord(long offset, String key, String payload, List<Header> headersList) {
        return new ConsumerRecord<>(TOPIC, PARTITION, offset, TIMESTAMP, TIMESTAMP_TYPE, 0L, 0, 0, key, payload, new RecordHeaders(headersList));
    }

    private KafkaBasicConsumeTask givenKafkaConsumeTask(String selector, String mimeType, String timeout) {
        return givenKafkaConsumeTask(1, selector, null, mimeType, timeout);
    }

    private KafkaBasicConsumeTask givenKafkaConsumeTask(int expectedMessageNb, String selector, String headerSelector, String mimeType, String timeout) {
        return new KafkaBasicConsumeTask(TARGET_STUB, TOPIC, GROUP, emptyMap(), expectedMessageNb, selector, headerSelector, mimeType, timeout, logger);
    }

    private void givenTaskReceiveMessages(Task task, ConsumerRecord<String, String>... messages) {
        MessageListener<String, String> listener = overrideTaskMessageListenerContainer(task);
        stream(messages).forEach(listener::onMessage);
    }

    private List<Map<String, Object>> assertTaskOutputsSize(TaskExecutionResult taskExecutionResult, int size) {
        assertThat(taskExecutionResult.outputs).hasSize(3);

        final List<Map<String, Object>> body = (List<Map<String, Object>>) taskExecutionResult.outputs.get(OUTPUT_BODY);
        final List<Map<String, Object>> payloads = (List<Map<String, Object>>) taskExecutionResult.outputs.get(OUTPUT_PAYLOADS);
        final List<Map<String, Object>> headers = (List<Map<String, Object>>) taskExecutionResult.outputs.get(OUTPUT_HEADERS);
        assertThat(body).hasSize(size);
        assertThat(payloads).hasSize(size);
        assertThat(headers).hasSize(size);

        Map<String, Object> bodyTmp;
        for (int i = 0; i < body.size(); i++) {
            bodyTmp = body.get(i);
            assertThat(bodyTmp.get(OUTPUT_BODY_PAYLOAD_KEY)).isEqualTo(payloads.get(i));
            assertThat(bodyTmp.get(OUTPUT_BODY_HEADERS_KEY)).isEqualTo(headers.get(i));
        }

        return body;
    }
}
