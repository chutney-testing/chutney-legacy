package com.chutneytesting.task.kafka;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Failure;
import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static java.util.Collections.emptyMap;
import static java.util.Collections.shuffle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.TestTarget;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Target;
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
public class KafkaBasicPublishTaskTest {

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
        KafkaBasicPublishTask defaultTask = new KafkaBasicPublishTask(null, null, null, null, null, null);
        assertThat(defaultTask)
            .hasFieldOrPropertyWithValue("topic", null)
            .hasFieldOrPropertyWithValue("headers", emptyMap())
            .hasFieldOrPropertyWithValue("payload", null)
            .hasFieldOrPropertyWithValue("properties", emptyMap())
        ;
    }

    @Test
    void should_validate_all_mandatory_inputs() {
        KafkaBasicPublishTask defaultTask = new KafkaBasicPublishTask(null, null, null, null, null, null);
        List<String> errors = defaultTask.validateInputs();

        assertThat(errors.size()).isEqualTo(9);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(errors.get(0)).isEqualTo("No topic provided (String)");
        softly.assertThat(errors.get(1)).isEqualTo("topic should not be blank");

        softly.assertThat(errors.get(2)).isEqualTo("No payload provided (String)");
        softly.assertThat(errors.get(3)).isEqualTo("payload should not be blank");

        softly.assertThat(errors.get(4)).isEqualTo("No target provided");
        softly.assertThat(errors.get(5)).isEqualTo("[Target name is blank] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertThat(errors.get(6)).isEqualTo("[No url defined on the target] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertThat(errors.get(7)).isEqualTo("[Target url is not valid] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertThat(errors.get(8)).isEqualTo("[Target url has an undefined host] not applied because of exception java.lang.NullPointerException(null)");

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

        KafkaBasicPublishTask defaultTask = new KafkaBasicPublishTask(target, null, null, null, properties, null);
        assertThat(defaultTask)
            .hasFieldOrPropertyWithValue("properties", expectedConfig)
        ;
    }

    @Test
    public void basic_publish_task_should_success() throws Exception {
        //given
        TestLogger logger = new TestLogger();
        Task task = new KafkaBasicPublishTask(getKafkaTarget(), TOPIC, null, PAYLOAD, null, logger);
        //mocks
        ChutneyKafkaProducerFactory producerFactoryMock = mock(ChutneyKafkaProducerFactory.class);
        KafkaTemplate<String, String> kafkaTemplateMock = mock(KafkaTemplate.class);
        when(producerFactoryMock.create(any(), any())).thenReturn(kafkaTemplateMock);

        ListenableFuture<SendResult<String, String>> listenableFutureMock = mock(ListenableFuture.class);
        when(listenableFutureMock.get(anyLong(), any(TimeUnit.class))).thenReturn(null);
        when(kafkaTemplateMock.send(ArgumentMatchers.<ProducerRecord<String, String>>any())).thenReturn(listenableFutureMock);

        ReflectionTestUtils.setField(task, "producerFactory", producerFactoryMock);

        //when
        TaskExecutionResult taskExecutionResult = task.execute();

        //Then
        assertThat(taskExecutionResult.status).isEqualTo(Success);
        assertThat(logger.errors).isEmpty();
        verify(listenableFutureMock).get(anyLong(), any(TimeUnit.class));
    }

    @Test
    public void basic_publish_task_should_failed_when_timeout() throws Exception {
        //given
        TestLogger logger = new TestLogger();
        Task task = new KafkaBasicPublishTask(getKafkaTarget(), TOPIC, null, PAYLOAD, null, logger);
        //mocks
        ChutneyKafkaProducerFactory producerFactoryMock = mock(ChutneyKafkaProducerFactory.class);
        KafkaTemplate<String, String> kafkaTemplateMock = mock(KafkaTemplate.class);
        when(producerFactoryMock.create(any(), any())).thenReturn(kafkaTemplateMock);

        ListenableFuture<SendResult<String, String>> listenableFutureMock = mock(ListenableFuture.class);
        when(listenableFutureMock.get(anyLong(), any(TimeUnit.class))).thenThrow(TimeoutException.class);
        when(kafkaTemplateMock.send(ArgumentMatchers.<ProducerRecord<String, String>>any())).thenReturn(listenableFutureMock);

        ReflectionTestUtils.setField(task, "producerFactory", producerFactoryMock);

        //when
        TaskExecutionResult taskExecutionResult = task.execute();

        //Then
        assertThat(taskExecutionResult.status).isEqualTo(Failure);
        assertThat(logger.errors).isNotEmpty();
    }
}
