package com.chutneytesting.action.kafka;

import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Failure;
import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Success;
import static java.util.Collections.emptyMap;
import static java.util.Collections.shuffle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.concurrent.ListenableFuture;

@SuppressWarnings("unchecked")
public class KafkaBasicPublishActionTest {

    private static final String TOPIC = "topic";
    private static final String PAYLOAD = "payload";

    private Target getKafkaTarget() {
        return TestTarget.TestTargetBuilder.builder()
            .withTargetId("kafka")
            .withUrl("127.0.0.1:5555")
            .build();
    }

    @Test
    void should_set_inputs_default_values() {
        KafkaBasicPublishAction defaultAction = new KafkaBasicPublishAction(null, null, null, null, null, null);
        assertThat(defaultAction)
            .hasFieldOrPropertyWithValue("topic", null)
            .hasFieldOrPropertyWithValue("headers", emptyMap())
            .hasFieldOrPropertyWithValue("payload", null)
            .hasFieldOrPropertyWithValue("properties", emptyMap())
        ;
    }

    @Test
    void should_validate_all_mandatory_inputs() {
        KafkaBasicPublishAction defaultAction = new KafkaBasicPublishAction(null, null, null, null, null, null);
        List<String> errors = defaultAction.validateInputs();

        assertThat(errors.size()).isEqualTo(8);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(errors.get(0)).isEqualTo("No topic provided (String)");
        softly.assertThat(errors.get(1)).isEqualTo("topic should not be blank");

        softly.assertThat(errors.get(2)).isEqualTo("No payload provided (String)");
        softly.assertThat(errors.get(3)).isEqualTo("payload should not be blank");

        softly.assertThat(errors.get(4)).isEqualTo("No target provided");
        softly.assertThat(errors.get(5)).isEqualTo("[Target name is blank] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertThat(errors.get(6)).isEqualTo("[Target url is not valid: null target] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertThat(errors.get(7)).isEqualTo("[Target url has an undefined host: null target] not applied because of exception java.lang.NullPointerException(null)");

        softly.assertAll();
    }

    @Test
    void should_merge_kafka_producer_target_properties_with_input_properties() {
        List<String> producerConfigKeys = new ArrayList<>(ProducerConfig.configNames());
        shuffle(producerConfigKeys);
        String targetProperty = producerConfigKeys.get(0);
        String propertyToOverride = producerConfigKeys.get(1);
        String inputProperty = producerConfigKeys.get(2);

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

        KafkaBasicPublishAction defaultAction = new KafkaBasicPublishAction(target, null, null, null, properties, null);
        assertThat(defaultAction)
            .hasFieldOrPropertyWithValue("properties", expectedConfig)
        ;
    }

    @Test
    public void basic_publish_action_should_success() throws Exception {
        //given
        TestLogger logger = new TestLogger();
        Action action = new KafkaBasicPublishAction(getKafkaTarget(), TOPIC, null, PAYLOAD, null, logger);
        //mocks
        ChutneyKafkaProducerFactory producerFactoryMock = mock(ChutneyKafkaProducerFactory.class);
        KafkaTemplate<String, String> kafkaTemplateMock = mock(KafkaTemplate.class);
        when(producerFactoryMock.create(any(), any())).thenReturn(kafkaTemplateMock);

        ListenableFuture<SendResult<String, String>> listenableFutureMock = mock(ListenableFuture.class);
        when(listenableFutureMock.get(anyLong(), any(TimeUnit.class))).thenReturn(null);
        when(kafkaTemplateMock.send(ArgumentMatchers.<ProducerRecord<String, String>>any())).thenReturn(listenableFutureMock);

        ReflectionTestUtils.setField(action, "producerFactory", producerFactoryMock);

        //when
        ActionExecutionResult actionExecutionResult = action.execute();

        //Then
        assertThat(actionExecutionResult.status).isEqualTo(Success);
        assertThat(logger.errors).isEmpty();
        verify(listenableFutureMock).get(anyLong(), any(TimeUnit.class));
    }

    @Test
    public void basic_publish_action_should_failed_when_timeout() throws Exception {
        //given
        TestLogger logger = new TestLogger();
        Action action = new KafkaBasicPublishAction(getKafkaTarget(), TOPIC, null, PAYLOAD, null, logger);
        //mocks
        ChutneyKafkaProducerFactory producerFactoryMock = mock(ChutneyKafkaProducerFactory.class);
        KafkaTemplate<String, String> kafkaTemplateMock = mock(KafkaTemplate.class);
        when(producerFactoryMock.create(any(), any())).thenReturn(kafkaTemplateMock);

        ListenableFuture<SendResult<String, String>> listenableFutureMock = mock(ListenableFuture.class);
        when(listenableFutureMock.get(anyLong(), any(TimeUnit.class))).thenThrow(TimeoutException.class);
        when(kafkaTemplateMock.send(ArgumentMatchers.<ProducerRecord<String, String>>any())).thenReturn(listenableFutureMock);

        ReflectionTestUtils.setField(action, "producerFactory", producerFactoryMock);

        //when
        ActionExecutionResult actionExecutionResult = action.execute();

        //Then
        assertThat(actionExecutionResult.status).isEqualTo(Failure);
        assertThat(logger.errors).isNotEmpty();
    }
}
