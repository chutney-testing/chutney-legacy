package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.micrometer.MicrometerCounterTask.OUTPUT_COUNTER;
import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.TaskExecutionResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MicrometerCounterTaskTest {

    private MicrometerCounterTask sut;

    private final String COUNTER_NAME = "counterName";
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
    public void counter_increment_must_be_number() {
        assertThatThrownBy(() ->
            new MicrometerCounterTask(null, COUNTER_NAME, null, null, null, null, null, "no number")
        ).isExactlyInstanceOf(NumberFormatException.class);
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
        sut = new MicrometerCounterTask(new TestLogger(), COUNTER_NAME, null, null, null, null, null, null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
        assertThat(result.outputs).containsOnlyKeys(OUTPUT_COUNTER);
        assertThat(result.outputs)
            .extractingByKey(OUTPUT_COUNTER)
            .isInstanceOf(Counter.class);

        Counter outputCounter = (Counter) result.outputs.get(OUTPUT_COUNTER);
        assertThat(globalRegistry.find(COUNTER_NAME).counter()).isEqualTo(outputCounter);
        assertThat(meterRegistry.find(COUNTER_NAME).counter())
            .isNotNull()
            .extracting("id").isEqualTo(outputCounter.getId());
        assertThat(outputCounter.count()).isEqualTo(0);
    }

    @Test
    public void should_create_micrometer_counter_and_register_it_on_given_registry() {
        // Given
        MeterRegistry givenMeterRegistry = new SimpleMeterRegistry();
        sut = new MicrometerCounterTask(new TestLogger(), COUNTER_NAME, "description", "my unit", Lists.list("tag", "my tag value"), null, givenMeterRegistry, null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
        assertThat(result.outputs).containsOnlyKeys(OUTPUT_COUNTER);
        assertThat(result.outputs)
            .extractingByKey(OUTPUT_COUNTER)
            .isInstanceOf(Counter.class);

        Counter outputCounter = (Counter) result.outputs.get(OUTPUT_COUNTER);
        assertThat(globalRegistry.find(COUNTER_NAME).counters()).isEmpty();
        assertThat(meterRegistry.find(COUNTER_NAME).counters()).isEmpty();
        assertThat(givenMeterRegistry.find(COUNTER_NAME).counter()).isEqualTo(outputCounter);
        assertThat(outputCounter.getId().getDescription()).isEqualTo("description");
        assertThat(outputCounter.getId().getBaseUnit()).isEqualTo("my unit");
        assertThat(outputCounter.getId().getTag("tag")).isEqualTo("my tag value");
    }

    @Test
    public void should_create_and_increment_micrometer_counter() {
        // Given
        sut = new MicrometerCounterTask(new TestLogger(), COUNTER_NAME, null, null, null, null, null, "5.0");

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
        Counter givenCounter = meterRegistry.counter(COUNTER_NAME);
        givenCounter.increment(3);
        sut = new MicrometerCounterTask(new TestLogger(), null, null, null, null, givenCounter, null, "5.0");

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
        sut = new MicrometerCounterTask(logger, COUNTER_NAME, null, null, null, null, null, "5.0");

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndCounterObjectType(result);

        assertThat(logger.info).hasSize(2);
        assertThat(logger.info.get(0)).isEqualTo("Counter incremented by 5.0");
        assertThat(logger.info.get(1)).isEqualTo("Counter current count is 5.0");
    }

    private void assertSuccessAndCounterObjectType(TaskExecutionResult result) {
        assertThat(result.status).isEqualTo(Success);
        assertThat(result.outputs).containsOnlyKeys(OUTPUT_COUNTER);
        assertThat(result.outputs)
            .extractingByKey(OUTPUT_COUNTER)
            .isInstanceOf(Counter.class);
    }
}
