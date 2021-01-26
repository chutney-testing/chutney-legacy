package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.micrometer.MicrometerTimerStopTask.OUTPUT_TIMER_SAMPLE_DURATION;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.TaskExecutionResult;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class MicrometerTimerStopTaskTest extends MicrometerTaskTest {

    private MicrometerTimerStopTask sut;

    @Test
    public void timing_sample_is_mandatory() {
        assertThatThrownBy(() ->
            new MicrometerTimerStopTask(null, null, Timer.builder("timerName").register(new SimpleMeterRegistry()), null).execute()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void timer_is_mandatory() {
        assertThatThrownBy(() ->
            new MicrometerTimerStopTask(null, Timer.start(Clock.SYSTEM), null, null).execute()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_stop_timing_sample_and_create_given_timer_associated_record() {
        // Given
        Timer timer = Timer.builder("timerName").register(meterRegistry);
        sut = new MicrometerTimerStopTask(new TestLogger(), Timer.start(Clock.SYSTEM), timer, null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndDurationObjectType(result);

        Duration outputDuration = (Duration) result.outputs.get(OUTPUT_TIMER_SAMPLE_DURATION);
        assertThat(outputDuration.toNanos()).isPositive();
        assertThat(globalRegistry.find(timer.getId().getName()).timers()).isEmpty();
        assertThat(meterRegistry.find(timer.getId().getName()).timer()).isEqualTo(timer);
        assertThat(timer.count()).isEqualTo(1);

        // Multiple stop calls
        // When
        result = sut.execute();

        // Then
        assertSuccessAndDurationObjectType(result);
        assertThat(timer.count()).isEqualTo(2);
    }

    @Test
    public void should_log_timing_sample_duration_and_timer_statistics() {
        // Given
        TestLogger logger = new TestLogger();
        sut = new MicrometerTimerStopTask(logger, Timer.start(Clock.SYSTEM), Timer.builder("timerName").register(meterRegistry), null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndDurationObjectType(result);

        assertThat(logger.info).hasSize(5);
        assertThat(logger.info.get(0)).contains("stopped").contains("last for");
        assertThat(logger.info.get(1)).contains("Timer current total time is");
        assertThat(logger.info.get(2)).contains("Timer current max time is");
        assertThat(logger.info.get(3)).contains("Timer current mean time is");
        assertThat(logger.info.get(4)).isEqualTo("Timer current count is 1");
    }

    private void assertSuccessAndDurationObjectType(TaskExecutionResult result) {
        assertSuccessAndOutputObjectType(result, OUTPUT_TIMER_SAMPLE_DURATION, Duration.class);
    }
}
