package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.micrometer.MicrometerTimerTask.OUTPUT_TIMER;
import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.TaskExecutionResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.concurrent.TimeUnit;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MicrometerTimerTaskTest {

    private MicrometerTimerTask sut;

    private final String TIMER_NAME = "timerName";
    private MeterRegistry meterRegistry;

    @Before
    public void before() {
        meterRegistry = new SimpleMeterRegistry();
        globalRegistry.add(meterRegistry);
    }

    @After
    public void after() {
        globalRegistry.forEachMeter(globalRegistry::remove);
        globalRegistry.remove(meterRegistry);
    }

    @Test
    public void timer_name_is_mandatory_if_no_given_timer() {
        assertThatThrownBy(() ->
            new MicrometerTimerTask(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null).execute()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void timer_buffer_length_must_be_an_integer() {
        assertThatThrownBy(() ->
            new MicrometerTimerTask(null, TIMER_NAME, null, null, "not a integer", null, null, null, null, null, null, null, null, null, null, null)
        ).isExactlyInstanceOf(NumberFormatException.class);
    }

    @Test
    public void timer_expiry_must_be_a_duration() {
        assertThatThrownBy(() ->
            new MicrometerTimerTask(null, TIMER_NAME, null, null, null, "not a duration", null, null, null, null, null, null, null, null, null, null)
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void timer_max_value_must_be_a_duration() {
        assertThatThrownBy(() ->
            new MicrometerTimerTask(null, TIMER_NAME, null, null, null, null, "not a duration", null, null, null, null, null, null, null, null, null)
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void timer_min_value_must_be_a_duration() {
        assertThatThrownBy(() ->
            new MicrometerTimerTask(null, TIMER_NAME, null, null, null, null, null, "not a duration", null, null, null, null, null, null, null, null)
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void timer_percentile_precision_must_be_an_integer() {
        assertThatThrownBy(() ->
            new MicrometerTimerTask(null, TIMER_NAME, null, null, null, null, null, null, "not a integer", null, null, null, null, null, null, null)
        ).isExactlyInstanceOf(NumberFormatException.class);
    }

    @Test
    public void timer_percentiles_must_be_a_list_of_double() {
        assertThatThrownBy(() ->
            new MicrometerTimerTask(null, TIMER_NAME, null, null, null, null, null, null, null, null, "not a list of double", null, null, null, null, null)
        ).isExactlyInstanceOf(NumberFormatException.class);
    }

    @Test
    public void timer_sla_must_be_a_list_of_duration() {
        assertThatThrownBy(() ->
            new MicrometerTimerTask(null, TIMER_NAME, null, null, null, null, null, null, null, null, null, "not a list of duration", null, null, null, null)
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void timer_record_must_be_a_duration() {
        assertThatThrownBy(() ->
            new MicrometerTimerTask(null, TIMER_NAME, null, null, null, null, null, null, null, null, null, null, null, null, null, "not a duration")
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_create_micrometer_timer() {
        // Given
        sut = new MicrometerTimerTask(new TestLogger(), TIMER_NAME, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndTimerObjectType(result);

        Timer outputTimer = (Timer) result.outputs.get(OUTPUT_TIMER);
        assertThat(globalRegistry.find(TIMER_NAME).timer()).isEqualTo(outputTimer);
        assertThat(meterRegistry.find(TIMER_NAME).timer())
            .isNotNull()
            .extracting("id").isEqualTo(outputTimer.getId());
        assertThat(outputTimer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(0);
        assertThat(outputTimer.max(TimeUnit.MILLISECONDS)).isEqualTo(0);
        assertThat(outputTimer.mean(TimeUnit.MILLISECONDS)).isEqualTo(0);
        assertThat(outputTimer.count()).isEqualTo(0);
    }

    @Test
    public void should_create_micrometer_counter_and_register_it_on_given_registry() {
        // Given
        MeterRegistry givenMeterRegistry = new SimpleMeterRegistry();
        sut = new MicrometerTimerTask(new TestLogger(), TIMER_NAME, "description", Lists.list("tag", "my tag value"), null, null, null, null, null, null, null, null, null, givenMeterRegistry, null, null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndTimerObjectType(result);

        Timer outputTimer = (Timer) result.outputs.get(OUTPUT_TIMER);
        assertThat(globalRegistry.find(TIMER_NAME).timers()).isEmpty();
        assertThat(meterRegistry.find(TIMER_NAME).timers()).isEmpty();
        assertThat(givenMeterRegistry.find(TIMER_NAME).timer()).isEqualTo(outputTimer);
        assertThat(outputTimer.getId().getDescription()).isEqualTo("description");
        assertThat(outputTimer.getId().getTag("tag")).isEqualTo("my tag value");
    }

    @Test
    public void should_create_micrometer_timer_and_record_an_event() {
        // Given
        sut = new MicrometerTimerTask(new TestLogger(), TIMER_NAME, null, null, null, null, null, null, null, null, null, null, null, null, null, "3 s");

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndTimerObjectType(result);

        Timer outputTimer = (Timer) result.outputs.get(OUTPUT_TIMER);
        assertThat(outputTimer.totalTime(TimeUnit.SECONDS)).isEqualTo(3);
        assertThat(outputTimer.max(TimeUnit.SECONDS)).isEqualTo(3);
        assertThat(outputTimer.mean(TimeUnit.SECONDS)).isEqualTo(3);
        assertThat(outputTimer.count()).isEqualTo(1);
    }

    @Test
    public void should_record_an_event_with_given_timer() {
        // Given
        Timer givenTimer = meterRegistry.timer(TIMER_NAME);
        givenTimer.record(3, TimeUnit.SECONDS);
        sut = new MicrometerTimerTask(new TestLogger(), null, null, null, null, null, null, null, null, null, null, null, givenTimer, null, null, "6 s");

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndTimerObjectType(result);

        Timer outputTimer = (Timer) result.outputs.get(OUTPUT_TIMER);
        assertThat(outputTimer).isEqualTo(givenTimer);
        assertThat(outputTimer.totalTime(TimeUnit.SECONDS)).isEqualTo(9);
        assertThat(outputTimer.max(TimeUnit.SECONDS)).isEqualTo(6);
        assertThat(outputTimer.mean(TimeUnit.SECONDS)).isEqualTo(4.5);
        assertThat(outputTimer.count()).isEqualTo(2);
    }

    @Test
    public void should_log_timer_record_total_max_mean_and_count() {
        // Given
        TestLogger logger = new TestLogger();
        sut = new MicrometerTimerTask(logger, TIMER_NAME, null, null, null, null, null, null, null, null, null, null, null, null, null, "6 s");

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndTimerObjectType(result);

        assertThat(logger.info).hasSize(5);
        assertThat(logger.info.get(0)).isEqualTo("Timer updated by PT6S");
        assertThat(logger.info.get(1)).isEqualTo("Timer current total time is 6.0 SECONDS");
        assertThat(logger.info.get(2)).isEqualTo("Timer current max time is 6.0 SECONDS");
        assertThat(logger.info.get(3)).isEqualTo("Timer current mean time is 6.0 SECONDS");
        assertThat(logger.info.get(4)).isEqualTo("Timer current count is 1");
    }

    private void assertSuccessAndTimerObjectType(TaskExecutionResult result) {
        assertThat(result.status).isEqualTo(Success);
        assertThat(result.outputs).containsOnlyKeys(OUTPUT_TIMER);
        assertThat(result.outputs)
            .extractingByKey(OUTPUT_TIMER)
            .isInstanceOf(Timer.class);
    }
}
