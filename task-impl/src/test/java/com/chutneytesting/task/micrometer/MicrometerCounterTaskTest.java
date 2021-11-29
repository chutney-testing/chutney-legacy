package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.micrometer.MicrometerCounterTask.OUTPUT_COUNTER;
import static com.chutneytesting.task.micrometer.MicrometerTaskTestHelper.assertSuccessAndOutputObjectType;
import static com.chutneytesting.task.micrometer.MicrometerTaskTestHelper.buildMeterName;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.TaskExecutionResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

public class MicrometerCounterTaskTest {

    private MicrometerCounterTask sut;
    private final String METER_NAME_PREFIX = "counterName";

    @Test
    public void counter_increment_must_be_number() {
        MicrometerCounterTask no_number = new MicrometerCounterTask(null, buildMeterName(METER_NAME_PREFIX), null, null, null, null, null, "no number");

        List<String> errors = no_number.validateInputs();

        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).isEqualTo("[increment parsing] not applied because of exception java.lang.NumberFormatException(For input string: \"no number\")");
    }

    @Test
    public void counter_name_is_mandatory_if_no_given_counter() {
        assertThatThrownBy(() ->
            new MicrometerCounterTask(null, null, null, null, null, null, null, null).execute()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_create_micrometer_counter() {
        // Given
        MeterRegistry registry = new SimpleMeterRegistry();
        String meterName = buildMeterName(METER_NAME_PREFIX);
        sut = new MicrometerCounterTask(new TestLogger(), meterName, null, null, null, null, registry, null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndCounterObjectType(result);

        Counter outputCounter = (Counter) result.outputs.get(OUTPUT_COUNTER);
        assertThat(registry.find(meterName).counter()).isEqualTo(outputCounter);
        assertThat(outputCounter.count()).isEqualTo(0);
    }

    @Test
    public void should_create_micrometer_counter_and_register_it_on_given_registry() {
        // Given
        MeterRegistry givenMeterRegistry = new SimpleMeterRegistry();
        String meterName = buildMeterName(METER_NAME_PREFIX);
        sut = new MicrometerCounterTask(new TestLogger(), meterName, "description", "my unit", Lists.list("tag", "my tag value"), null, givenMeterRegistry, null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndCounterObjectType(result);

        Counter outputCounter = (Counter) result.outputs.get(OUTPUT_COUNTER);
        assertThat(globalRegistry.find(meterName).counters()).isEmpty();
        assertThat(givenMeterRegistry.find(meterName).counter()).isEqualTo(outputCounter);
        assertThat(outputCounter.getId().getDescription()).isEqualTo("description");
        assertThat(outputCounter.getId().getBaseUnit()).isEqualTo("my unit");
        assertThat(outputCounter.getId().getTag("tag")).isEqualTo("my tag value");
    }

    @Test
    public void should_create_and_increment_micrometer_counter() {
        // Given
        MeterRegistry registry = new SimpleMeterRegistry();
        sut = new MicrometerCounterTask(new TestLogger(), buildMeterName(METER_NAME_PREFIX), null, null, null, null, registry, "5.0");

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndCounterObjectType(result);

        Counter outputCounter = (Counter) result.outputs.get(OUTPUT_COUNTER);
        assertThat(outputCounter.count()).isEqualTo(5);
    }

    @Test
    public void should_increment_given_counter() {
        // Given
        MeterRegistry registry = new SimpleMeterRegistry();
        Counter givenCounter = registry.counter(buildMeterName(METER_NAME_PREFIX));
        givenCounter.increment(3);
        sut = new MicrometerCounterTask(new TestLogger(), null, null, null, null, givenCounter, registry, "5.0");

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndCounterObjectType(result);

        Counter outputCounter = (Counter) result.outputs.get(OUTPUT_COUNTER);
        assertThat(outputCounter).isEqualTo(givenCounter);
        assertThat(outputCounter.count()).isEqualTo(8);
    }

    @Test
    public void should_log_increment_and_current_count() {
        // Given
        TestLogger logger = new TestLogger();
        sut = new MicrometerCounterTask(logger, buildMeterName(METER_NAME_PREFIX), null, null, null, null, new SimpleMeterRegistry(), "5.0");

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndCounterObjectType(result);

        assertThat(logger.info).hasSize(2);
        assertThat(logger.info.get(0)).isEqualTo("Counter incremented by 5.0");
        assertThat(logger.info.get(1)).isEqualTo("Counter current count is 5.0");
    }

    private void assertSuccessAndCounterObjectType(TaskExecutionResult result) {
        assertSuccessAndOutputObjectType(result, OUTPUT_COUNTER, Counter.class);
    }
}
