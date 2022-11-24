package com.chutneytesting.action.micrometer;

import static com.chutneytesting.action.micrometer.MicrometerActionTestHelper.assertSuccessAndOutputObjectType;
import static com.chutneytesting.action.micrometer.MicrometerActionTestHelper.buildMeterName;
import static com.chutneytesting.action.micrometer.MicrometerTimerStopAction.OUTPUT_TIMER_SAMPLE_DURATION;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.spi.ActionExecutionResult;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class MicrometerTimerStopActionTest {

    private MicrometerTimerStopAction sut;
    public static final String METER_NAME_PREFIX = "timerName";

    @Test
    public void timing_sample_is_mandatory() {
        assertThatThrownBy(() ->
            new MicrometerTimerStopAction(null, null, Timer.builder(buildMeterName(METER_NAME_PREFIX)).register(new SimpleMeterRegistry()), null).execute()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void timer_is_mandatory() {
        assertThatThrownBy(() ->
            new MicrometerTimerStopAction(null, Timer.start(Clock.SYSTEM), null, null).execute()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_stop_timing_sample_and_create_given_timer_associated_record() {
        // Given
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        globalRegistry.add(meterRegistry);
        Timer timer = Timer.builder(buildMeterName(METER_NAME_PREFIX)).register(meterRegistry);
        sut = new MicrometerTimerStopAction(new TestLogger(), Timer.start(Clock.SYSTEM), timer, null);

        // When
        ActionExecutionResult result = sut.execute();

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
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        globalRegistry.add(meterRegistry);
        sut = new MicrometerTimerStopAction(logger, Timer.start(Clock.SYSTEM), Timer.builder(buildMeterName(METER_NAME_PREFIX)).register(meterRegistry), null);

        // When
        ActionExecutionResult result = sut.execute();

        // Then
        assertSuccessAndDurationObjectType(result);

        assertThat(logger.info).hasSize(5);
        assertThat(logger.info.get(0)).contains("stopped").contains("last for");
        assertThat(logger.info.get(1)).contains("Timer current total time is");
        assertThat(logger.info.get(2)).contains("Timer current max time is");
        assertThat(logger.info.get(3)).contains("Timer current mean time is");
        assertThat(logger.info.get(4)).isEqualTo("Timer current count is 1");
    }

    private void assertSuccessAndDurationObjectType(ActionExecutionResult result) {
        assertSuccessAndOutputObjectType(result, OUTPUT_TIMER_SAMPLE_DURATION, Duration.class);
    }
}
