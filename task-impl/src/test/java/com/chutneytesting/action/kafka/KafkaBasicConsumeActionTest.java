package com.chutneytesting.action.kafka;

import static com.chutneytesting.action.kafka.KafkaBasicConsumeAction.OUTPUT_BODY;
import static com.chutneytesting.action.kafka.KafkaBasicConsumeAction.OUTPUT_BODY_HEADERS_KEY;
import static com.chutneytesting.action.kafka.KafkaBasicConsumeAction.OUTPUT_BODY_PAYLOAD_KEY;
import static com.chutneytesting.action.kafka.KafkaBasicConsumeAction.OUTPUT_HEADERS;
import static com.chutneytesting.action.kafka.KafkaBasicConsumeAction.OUTPUT_PAYLOADS;
import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Failure;
import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Success;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.Collections.shuffle;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_XML_VALUE;
import static org.springframework.util.MimeTypeUtils.TEXT_PLAIN_VALUE;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Target;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MimeType;
import wiremock.com.google.common.collect.ImmutableMap;

@SuppressWarnings("unchecked")
public class KafkaBasicConsumeActionTest {

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

    @BeforeEach
    public void before() {
        logger = new TestLogger();
    }

    @Test
    void should_set_inputs_default_values() {
        KafkaBasicConsumeAction defaultAction = new KafkaBasicConsumeAction(null, null, null, null, null, null, null, null, null, null, null);
        assertThat(defaultAction)
            .hasFieldOrPropertyWithValue("topic", null)
            .hasFieldOrPropertyWithValue("group", null)
            .hasFieldOrPropertyWithValue("properties", emptyMap())
            .hasFieldOrPropertyWithValue("nbMessages", 1)
            .hasFieldOrPropertyWithValue("selector", null)
            .hasFieldOrPropertyWithValue("headerSelector", null)
            .hasFieldOrPropertyWithValue("contentType", MimeType.valueOf("application/json"))
            .hasFieldOrPropertyWithValue("timeout", "60 sec")
            .hasFieldOrPropertyWithValue("ackMode", "BATCH")
        ;
    }

    @Test
    void should_validate_all_mandatory_inputs() {
        KafkaBasicConsumeAction defaultAction = new KafkaBasicConsumeAction(null, null, null, null, null, null, null, null, null, null, null);
        List<String> errors = defaultAction.validateInputs();

        assertThat(errors.size()).isEqualTo(8);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(errors.get(0)).isEqualTo("No topic provided (String)");
        softly.assertThat(errors.get(1)).isEqualTo("topic should not be blank");

        softly.assertThat(errors.get(2)).isEqualTo("No group provided (String)");
        softly.assertThat(errors.get(3)).isEqualTo("group should not be blank");

        softly.assertThat(errors.get(4)).isEqualTo("No target provided");
        softly.assertThat(errors.get(5)).isEqualTo("[Target name is blank] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertThat(errors.get(6)).isEqualTo("[Target url is not valid: null target] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertThat(errors.get(7)).isEqualTo("[Target url has an undefined host: null target] not applied because of exception java.lang.NullPointerException(null)");

        softly.assertAll();
    }

    @Test
    void should_validate_timeout_input() {
        String badTimeout = "twenty seconds";
        KafkaBasicConsumeAction defaultAction = new KafkaBasicConsumeAction(TARGET_STUB, "topic", "group", null, null, null, null, null, badTimeout, null, null);

        List<String> errors = defaultAction.validateInputs();

        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).startsWith("[timeout is not parsable]");
    }

    @Test
    void should_validate_ackMode_input() {
        String badTackMode = "UNKNOWN_ACKMODE";
        KafkaBasicConsumeAction defaultAction = new KafkaBasicConsumeAction(TARGET_STUB, "topic", "group", null, null, null, null, null, null, badTackMode, null);

        List<String> errors = defaultAction.validateInputs();

        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).startsWith("ackMode is not a valid value");
    }

    @Test
    void should_merge_kafka_consumer_target_properties_with_input_properties() {
        List<String> consumerConfigKeys = new ArrayList<>(ConsumerConfig.configNames());
        shuffle(consumerConfigKeys);
        String targetProperty = consumerConfigKeys.get(0);
        String propertyToOverride = consumerConfigKeys.get(1);
        String inputProperty = consumerConfigKeys.get(2);

        Target target = TestTarget.TestTargetBuilder.builder()
            .withProperty(targetProperty, "a value")
            .withProperty(propertyToOverride, "a target value")
            .build();

        Map<String, String> properties = Map.of(
            inputProperty, "a VALUE",
            propertyToOverride, "a property value"
        );

        Map<String, String> expectedConfig = Map.of(
            targetProperty, "a value",
            inputProperty, "a VALUE",
            propertyToOverride, "a property value"
        );

        KafkaBasicConsumeAction defaultAction = new KafkaBasicConsumeAction(target, null, null, properties, null, null, null, null, null, null, null);
        assertThat(defaultAction)
            .hasFieldOrPropertyWithValue("properties", expectedConfig)
        ;
    }

    @Test
    public void should_consume_simple_text_message() {
        // Given
        Action sut = givenKafkaConsumeAction(null, TEXT_PLAIN_VALUE, null);
        givenActionReceiveMessages(sut,
            buildRecord(FIRST_OFFSET, "KEY", "test message")
        );

        // When
        ActionExecutionResult actionExecutionResult = sut.execute();

        // Then
        assertThat(actionExecutionResult.status).isEqualTo(Success);
        List<Map<String, Object>> body = assertActionOutputsSize(actionExecutionResult, 1);

        final Map<String, Object> message = body.get(0);
        final String payload1 = (String) message.get(OUTPUT_BODY_PAYLOAD_KEY);
        assertThat(payload1).isEqualTo("test message");
        final Map<String, Object> headers = (Map<String, Object>) message.get(OUTPUT_BODY_HEADERS_KEY);
        assertThat(headers.get("X-Custom-HeaderKey")).isEqualTo("X-Custom-HeaderValue");
        assertThat(headers).containsAllEntriesOf(ImmutableMap.of("X-Custom-HeaderKey", "X-Custom-HeaderValue", "header1", "value1"));

        assertThat(logger.errors).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = APPLICATION_JSON_VALUE)
    @NullSource
    public void should_consume_json_message_as_map(String mimeType) {
        // Given
        Action action = givenKafkaConsumeAction(null, mimeType, null);
        givenActionReceiveMessages(action,
            buildRecord(FIRST_OFFSET, "KEY", "{\"value\": \"test message\", \"id\": \"1111\" }")
        );

        // When
        ActionExecutionResult actionExecutionResult = action.execute();

        // Then
        assertThat(actionExecutionResult.status).isEqualTo(Success);

        final Map<String, Object> payload = ((List<Map<String, Object>>) actionExecutionResult.outputs.get(OUTPUT_PAYLOADS)).get(0);
        assertThat(payload.get("value")).isEqualTo("test message");
        assertThat(payload.get("id")).isEqualTo("1111");
    }

    @Test
    public void should_consume_xml_message_as_string() {
        // Given
        Action action = givenKafkaConsumeAction(null, APPLICATION_XML_VALUE, null);
        String xmlPayload = "<root><first>first content</first><second attr=\"second attr\">second content</second></root>";
        givenActionReceiveMessages(action,
            buildRecord(FIRST_OFFSET, "KEY", xmlPayload)
        );

        // When
        ActionExecutionResult actionExecutionResult = action.execute();

        // Then
        assertThat(actionExecutionResult.status).isEqualTo(Success);

        final String payload = ((List<String>) actionExecutionResult.outputs.get(OUTPUT_PAYLOADS)).get(0);
        assertThat(payload).isEqualTo(xmlPayload);
    }

    @Test
    public void should_select_json_message_whose_payload_or_headers_match_given_payload_jsonpath_selector() {
        // Given
        Action action = givenKafkaConsumeAction("$..[?($.headers.header1=='value1' && $.payload.id==\"1122\")]", APPLICATION_JSON_VALUE, null);
        givenActionReceiveMessages(action,
            buildRecord(FIRST_OFFSET, "KEY1", "{\"value\": \"test message1\", \"id\": \"1111\" }"),
            buildRecord(FIRST_OFFSET + 1, "KEY2", "{\"value\": \"test message2\", \"id\": \"1122\" }"),
            buildRecord(FIRST_OFFSET + 2, "KEY3", "{\"value\": \"test message3\", \"id\": \"1133\" }")
        );

        // When
        ActionExecutionResult actionExecutionResult = action.execute();

        // Then
        assertThat(actionExecutionResult.status).isEqualTo(Success);

        List<Map<String, Object>> body = (List<Map<String, Object>>) actionExecutionResult.outputs.get(OUTPUT_BODY);
        assertThat(body).hasSize(1);
        final Map<String, Object> payload = ((List<Map<String, Object>>) actionExecutionResult.outputs.get(OUTPUT_PAYLOADS)).get(0);
        assertThat(payload.get("id")).isEqualTo("1122");
    }

    @Test
    public void should_select_xml_message_whose_payload_match_given_payload_xpath_selector() {
        // Given
        Action action = givenKafkaConsumeAction("/root/second[@attr='1122']", APPLICATION_XML_VALUE, null);
        String payloadToSelect = "<root><first>first content</first><second attr=\"1122\">second content</second></root>";
        givenActionReceiveMessages(action,
            buildRecord(FIRST_OFFSET, "KEY1", "<root><first>first content</first><second attr=\"1111\">second content</second></root>"),
            buildRecord(FIRST_OFFSET + 1, "KEY2", payloadToSelect),
            buildRecord(FIRST_OFFSET + 2, "KEY3", "<root><first>first content</first><second attr=\"1133\">second content</second></root>")
        );

        // When
        ActionExecutionResult actionExecutionResult = action.execute();

        // Then
        assertThat(actionExecutionResult.status).isEqualTo(Success);

        List<Map<String, Object>> body = (List<Map<String, Object>>) actionExecutionResult.outputs.get(OUTPUT_BODY);
        assertThat(body).hasSize(1);
        final String payload = ((List<String>) actionExecutionResult.outputs.get(OUTPUT_PAYLOADS)).get(0);
        assertThat(payload).isEqualTo(payloadToSelect);
    }

    @Test
    public void should_select_text_message_whose_payload_contains_given_payload_selector() {
        // Given
        Action action = givenKafkaConsumeAction("selector", TEXT_PLAIN_VALUE, null);
        String payloadToSelect = "second text selector message";
        givenActionReceiveMessages(action,
            buildRecord(FIRST_OFFSET, "KEY1", "first text message"),
            buildRecord(FIRST_OFFSET + 1, "KEY2", payloadToSelect),
            buildRecord(FIRST_OFFSET + 2, "KEY3", "third text message")
        );

        // When
        ActionExecutionResult actionExecutionResult = action.execute();

        // Then
        assertThat(actionExecutionResult.status).isEqualTo(Success);

        List<Map<String, Object>> body = (List<Map<String, Object>>) actionExecutionResult.outputs.get(OUTPUT_BODY);
        assertThat(body).hasSize(1);
        final String payload = ((List<String>) actionExecutionResult.outputs.get(OUTPUT_PAYLOADS)).get(0);
        assertThat(payload).isEqualTo(payloadToSelect);
    }

    @Test
    public void should_select_message_whose_headers_match_given_payload_jsonpath_selector() {
        // Given
        ImmutableList<Header> headers = ImmutableList.of(
            new RecordHeader("X-Custom-HeaderKey", "X-Custom-HeaderValue".getBytes()),
            new RecordHeader("header", "666".getBytes())
        );
        Action action = givenKafkaConsumeAction(3, null, "$..[?($.header=='666')]", null, null);
        String textMessageToSelect = "first text message";
        String xmlMessageToSelect = "<root>first xml message</root>";
        String jsonMessageToSelect = "first json message";
        givenActionReceiveMessages(action,
            buildRecord(FIRST_OFFSET, "KEY1", textMessageToSelect, headers),
            buildRecord(FIRST_OFFSET + 1, "KEY2", "second text message"),
            buildRecord(FIRST_OFFSET + 2, "KEY3", xmlMessageToSelect, headers),
            buildRecord(FIRST_OFFSET + 3, "KEY4", "<root>second xml message</root>"),
            buildRecord(FIRST_OFFSET + 4, "KEY5", "{\"value\": \"" + jsonMessageToSelect + "\", \"id\": \"1\" }", headers),
            buildRecord(FIRST_OFFSET + 5, "KEY6", "{\"value\": \"second json message\", \"id\": \"1\" }")
        );

        // When
        ActionExecutionResult actionExecutionResult = action.execute();

        // Then
        assertThat(actionExecutionResult.status).isEqualTo(Success);
        List<Map<String, Object>> body = assertActionOutputsSize(actionExecutionResult, 3);

        final String payload = (String) body.get(0).get(OUTPUT_BODY_PAYLOAD_KEY);
        assertThat(payload).isEqualTo(textMessageToSelect);
        final String xmlPayload = (String) body.get(1).get(OUTPUT_BODY_PAYLOAD_KEY);
        assertThat(xmlPayload).isEqualTo(xmlMessageToSelect);
        final Map<String, String> jsonPayload = (Map<String, String>) body.get(2).get(OUTPUT_BODY_PAYLOAD_KEY);
        assertThat(jsonPayload.get("value")).isEqualTo(jsonMessageToSelect);
    }

    @Test
    public void should_consume_message_with_duplicated_header_pair_key_value() {
        // Given
        ImmutableList<Header> headers = ImmutableList.of(
            new RecordHeader("header", "666".getBytes()),
            new RecordHeader("header", "666".getBytes())
        );
        Action action = givenKafkaConsumeAction(1, null, "$..[?($.header=='666')]", null, null);
        String textMessageToSelect = "first text message";
        givenActionReceiveMessages(action,
            buildRecord(FIRST_OFFSET, "KEY1", textMessageToSelect, headers),
            buildRecord(FIRST_OFFSET + 1, "KEY2", "second text message")
        );

        // When
        ActionExecutionResult actionExecutionResult = action.execute();

        // Then
        assertThat(actionExecutionResult.status).isEqualTo(Success);
        List<Map<String, Object>> body = assertActionOutputsSize(actionExecutionResult, 1);

        final String payload = (String) body.get(0).get(OUTPUT_BODY_PAYLOAD_KEY);
        assertThat(payload).isEqualTo(textMessageToSelect);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Content-Type", "Contenttype", "content type"})
    public void should_override_given_mime_type_by_message_header(String contentTypeHeaderKey) {
        // Given
        ImmutableList<Header> headers = ImmutableList.of(
            new RecordHeader("X-Custom-HeaderKey", "X-Custom-HeaderValue".getBytes()),
            new RecordHeader(contentTypeHeaderKey, APPLICATION_JSON_VALUE.getBytes())
        );
        Action action = givenKafkaConsumeAction(null, APPLICATION_XML_VALUE, null);
        givenActionReceiveMessages(action,
            buildRecord(FIRST_OFFSET, "KEY1", "{\"value\": \"test message\", \"id\": \"1\" }", headers)
        );

        // When
        ActionExecutionResult actionExecutionResult = action.execute();

        // Then
        assertThat(actionExecutionResult.status).isEqualTo(Success);
        List<Map<String, Object>> body = assertActionOutputsSize(actionExecutionResult, 1);

        assertThat(logger.info).contains("Found content type header " + APPLICATION_JSON_VALUE);

        final Map<String, String> payload = (Map<String, String>) body.get(0).get(OUTPUT_BODY_PAYLOAD_KEY);
        assertThat(payload)
            .containsEntry("value", "test message")
            .containsEntry("id", "1");
    }

    @Test
    public void should_respect_given_timeout() {
        // Given
        Action action = givenKafkaConsumeAction(null, null, "3 sec");
        overrideActionMessageListenerContainer(action);

        // When
        ActionExecutionResult actionExecutionResult = action.execute();

        // Then
        assertThat(actionExecutionResult.status).isEqualTo(Failure);
        assertThat(logger.errors).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"bad content type", APPLICATION_JSON_VALUE, "\"" + APPLICATION_JSON_VALUE + "\""})
    public void should_consume_as_json_with_bad_content_type_in_received_message(String contentType) {
        ImmutableList<Header> headers = ImmutableList.of(
            new RecordHeader("Content-type", contentType.getBytes())
        );
        // Given
        Action action = givenKafkaConsumeAction(null, null, null);
        givenActionReceiveMessages(action,
            buildRecord(FIRST_OFFSET, "KEY", "{\"value\": \"test message\", \"id\": \"1111\" }", headers)
        );

        // When
        ActionExecutionResult actionExecutionResult = action.execute();

        // Then
        assertThat(actionExecutionResult.status).isEqualTo(Success);

        final Map<String, Object> payload = ((List<Map<String, Object>>) actionExecutionResult.outputs.get(OUTPUT_PAYLOADS)).get(0);
        assertThat(payload.get("value")).isEqualTo("test message");
        assertThat(payload.get("id")).isEqualTo("1111");
    }

    private MessageListener<String, String> overrideActionMessageListenerContainer(Action action) {
        ConsumerFactory<String, String> cf = mock(ConsumerFactory.class, RETURNS_DEEP_STUBS);
        Consumer<String, String> consumer = mock(Consumer.class);
        given(cf.createConsumer(any(), any(), any(), any())).willReturn(consumer);
        when(cf.getConfigurationProperties().get(eq(ConsumerConfig.GROUP_ID_CONFIG))).thenReturn(GROUP);

        KafkaConsumerFactoryFactory kafkaConsumerFactoryFactory = mock(KafkaConsumerFactoryFactory.class);
        when(kafkaConsumerFactoryFactory.create(any(), any(), any())).thenReturn(cf);
        ReflectionTestUtils.setField(action, "kafkaConsumerFactoryFactory", kafkaConsumerFactoryFactory);

        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        containerProperties.setGroupId(GROUP);
        containerProperties.setMessageListener(requireNonNull(ReflectionTestUtils.invokeMethod(action, "createMessageListener")));
        ConcurrentMessageListenerContainer<String, String> messageListenerContainer = new ConcurrentMessageListenerContainer<>(cf, containerProperties);

        return (MessageListener<String, String>) messageListenerContainer.getContainerProperties().getMessageListener();
    }

    private ConsumerRecord<String, String> buildRecord(long offset, String key, String payload) {
        List<Header> headersList = ImmutableList.of(new RecordHeader("X-Custom-HeaderKey", "X-Custom-HeaderValue".getBytes()), new RecordHeader("header1", "value1".getBytes()));
        return new ConsumerRecord<>(TOPIC, PARTITION, offset, TIMESTAMP, TIMESTAMP_TYPE, 0L, 0, 0, key, payload, new RecordHeaders(headersList));
    }

    private ConsumerRecord<String, String> buildRecord(long offset, String key, String payload, List<Header> headersList) {
        return new ConsumerRecord<>(TOPIC, PARTITION, offset, TIMESTAMP, TIMESTAMP_TYPE, 0L, 0, 0, key, payload, new RecordHeaders(headersList));
    }

    private KafkaBasicConsumeAction givenKafkaConsumeAction(String selector, String mimeType, String timeout) {
        return givenKafkaConsumeAction(1, selector, null, mimeType, timeout);
    }

    private KafkaBasicConsumeAction givenKafkaConsumeAction(int expectedMessageNb, String selector, String headerSelector, String mimeType, String timeout) {
        return new KafkaBasicConsumeAction(TARGET_STUB, TOPIC, GROUP, emptyMap(), expectedMessageNb, selector, headerSelector, mimeType, timeout, null, logger);
    }

    private void givenActionReceiveMessages(Action action, ConsumerRecord<String, String>... messages) {
        MessageListener<String, String> listener = overrideActionMessageListenerContainer(action);
        stream(messages).forEach(listener::onMessage);
    }

    private List<Map<String, Object>> assertActionOutputsSize(ActionExecutionResult actionExecutionResult, int size) {
        assertThat(actionExecutionResult.outputs).hasSize(3);

        final List<Map<String, Object>> body = (List<Map<String, Object>>) actionExecutionResult.outputs.get(OUTPUT_BODY);
        final List<Map<String, Object>> payloads = (List<Map<String, Object>>) actionExecutionResult.outputs.get(OUTPUT_PAYLOADS);
        final List<Map<String, Object>> headers = (List<Map<String, Object>>) actionExecutionResult.outputs.get(OUTPUT_HEADERS);
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
