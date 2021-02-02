package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.micrometer.MicrometerTimerStartTask.OUTPUT_TIMER_SAMPLE;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.TaskExecutionResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

public class MicrometerTimerStartTaskTest extends MicrometerTaskTest {

    private MicrometerTimerStartTask sut;

    @Test
    public void should_start_a_timing_sample() {
        // Given
        sut = new MicrometerTimerStartTask(new TestLogger(), null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndSampleObjectType(result);

        Timer.Sample timerSampleOutput = (Timer.Sample) result.outputs.get(OUTPUT_TIMER_SAMPLE);
        assertThat(globalRegistry.getMeters()).isEmpty();
        assertThat(meterRegistry.getMeters()).isEmpty();
        assertThat(timerSampleOutput).isNotNull();
    }

    @Test
    public void should_start_a_timing_sample_with_a_given_registry() {
        // Given
        MeterRegistry givenMeterRegistry = new SimpleMeterRegistry();
        sut = new MicrometerTimerStartTask(new TestLogger(), givenMeterRegistry);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndSampleObjectType(result);

        Timer.Sample timerSampleOutput = (Timer.Sample) result.outputs.get(OUTPUT_TIMER_SAMPLE);
        assertThat(globalRegistry.getMeters()).isEmpty();
        assertThat(meterRegistry.getMeters()).isEmpty();
        assertThat(givenMeterRegistry.getMeters()).isEmpty();
        assertThat(timerSampleOutput).isNotNull();
    }

    @Test
    public void should_log_timing_sample_start() {
        // Given
        TestLogger logger = new TestLogger();
        sut = new MicrometerTimerStartTask(logger, null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndSampleObjectType(result);

        assertThat(logger.info).hasSize(1);
        assertThat(logger.info.get(0)).contains("started");
    }

    private void assertSuccessAndSampleObjectType(TaskExecutionResult result) {
        assertSuccessAndOutputObjectType(result, OUTPUT_TIMER_SAMPLE, Timer.Sample.class);
    }
}
