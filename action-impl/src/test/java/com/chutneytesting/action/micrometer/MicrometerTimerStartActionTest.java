package com.chutneytesting.action.micrometer;

import static com.chutneytesting.action.micrometer.MicrometerActionTestHelper.assertSuccessAndOutputObjectType;
import static com.chutneytesting.action.micrometer.MicrometerTimerStartAction.OUTPUT_TIMER_SAMPLE;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.spi.ActionExecutionResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

public class MicrometerTimerStartActionTest {

    private MicrometerTimerStartAction sut;

    @Test
    public void should_start_a_timing_sample() {
        // Given
        sut = new MicrometerTimerStartAction(new TestLogger(), null);

        // When
        ActionExecutionResult result = sut.execute();

        // Then
        assertSuccessAndSampleObjectType(result);

        Timer.Sample timerSampleOutput = (Timer.Sample) result.outputs.get(OUTPUT_TIMER_SAMPLE);
        assertThat(timerSampleOutput).isNotNull();
    }

    @Test
    public void should_start_a_timing_sample_with_a_given_registry() {
        // Given
        MeterRegistry givenMeterRegistry = new SimpleMeterRegistry();
        sut = new MicrometerTimerStartAction(new TestLogger(), givenMeterRegistry);

        // When
        ActionExecutionResult result = sut.execute();

        // Then
        assertSuccessAndSampleObjectType(result);

        Timer.Sample timerSampleOutput = (Timer.Sample) result.outputs.get(OUTPUT_TIMER_SAMPLE);
        assertThat(givenMeterRegistry.getMeters()).isEmpty();
        assertThat(timerSampleOutput).isNotNull();
    }

    @Test
    public void should_log_timing_sample_start() {
        // Given
        TestLogger logger = new TestLogger();
        sut = new MicrometerTimerStartAction(logger, null);

        // When
        ActionExecutionResult result = sut.execute();

        // Then
        assertSuccessAndSampleObjectType(result);

        assertThat(logger.info).hasSize(1);
        assertThat(logger.info.get(0)).contains("started");
    }

    private void assertSuccessAndSampleObjectType(ActionExecutionResult result) {
        assertSuccessAndOutputObjectType(result, OUTPUT_TIMER_SAMPLE, Timer.Sample.class);
    }
}
