package com.chutneytesting.task.kafka;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Failure;
import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.TestTarget;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.concurrent.ListenableFuture;

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
    public void basic_publish_task_should_success() throws Exception {
        //given
        TestLogger logger = new TestLogger();
        Task task = new KafkaBasicPublishTask(getKafkaTarget(), TOPIC, null, PAYLOAD, logger);
        //mocks
        ChutneyKafkaProducerFactory producerFactoryMock = mock(ChutneyKafkaProducerFactory.class);
        KafkaTemplate kafkaTemplateMock = mock(KafkaTemplate.class);
        when(producerFactoryMock.create(any())).thenReturn(kafkaTemplateMock);

        ListenableFuture<SendResult> listenableFutureMock = mock(ListenableFuture.class);
        when(listenableFutureMock.get(anyLong(), any(TimeUnit.class))).thenReturn(null);
        when(kafkaTemplateMock.send(any(ProducerRecord.class))).thenReturn(listenableFutureMock);

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
        Task task = new KafkaBasicPublishTask(getKafkaTarget(), TOPIC, null, PAYLOAD, logger);
        //mocks
        ChutneyKafkaProducerFactory producerFactoryMock = mock(ChutneyKafkaProducerFactory.class);
        KafkaTemplate kafkaTemplateMock = mock(KafkaTemplate.class);
        when(producerFactoryMock.create(any())).thenReturn(kafkaTemplateMock);

        ListenableFuture<SendResult> listenableFutureMock = mock(ListenableFuture.class);
        when(listenableFutureMock.get(anyLong(), any(TimeUnit.class))).thenThrow(TimeoutException.class);
        when(kafkaTemplateMock.send(any(ProducerRecord.class))).thenReturn(listenableFutureMock);

        ReflectionTestUtils.setField(task, "producerFactory", producerFactoryMock);

        //when
        TaskExecutionResult taskExecutionResult = task.execute();

        //Then
        assertThat(taskExecutionResult.status).isEqualTo(Failure);
        assertThat(logger.errors).isNotEmpty();
    }

}
