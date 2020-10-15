package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.micrometer.MicrometerGaugeTask.OUTPUT_GAUGE;
import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Failure;
import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.TaskExecutionResult;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;
import org.apache.groovy.util.Maps;
import org.assertj.core.util.Lists;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class MicrometerGaugeTaskTest extends MicrometerTaskTest {

    private MicrometerGaugeTask sut;
    private final String GAUGE_NAME = "gaugeName";

    @Test
    public void gauge_name_is_mandatory() {
        assertThatThrownBy(() ->
            new MicrometerGaugeTask(null, null, null, null, null, null, null, null, null)
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void gauge_object_and_gauge_function_must_not_be_both_null() {
        assertThatThrownBy(() ->
            new MicrometerGaugeTask(null, GAUGE_NAME, null, null, null, null, null, null, null)
        ).isExactlyInstanceOf(IllegalArgumentException.class).hasMessageContaining("both null");
    }

    @Test
    public void gauge_function_must_be_fully_qualified_when_gauge_object_null() {
        // Given
        TestLogger logger = new TestLogger();
        sut = new MicrometerGaugeTask(logger, GAUGE_NAME, null, null, null, null, null, "size", null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertOneError(result, logger, "cannot be resolved");
    }

    @Test
    public void gauge_function_must_be_static_when_gauge_object_null() {
        // Given
        TestLogger logger = new TestLogger();
        sut = new MicrometerGaugeTask(logger, GAUGE_NAME, null, null, null, null, null, "java.util.ArrayList.size", null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertOneError(result, logger, "must be static");
    }

    @Test
    public void gauge_function_must_have_no_parameters() {
        // Given
        TestLogger logger = new TestLogger();
        sut = new MicrometerGaugeTask(logger, GAUGE_NAME, null, null, null, null, new ArrayList<>(), "java.util.ArrayList.get", null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertOneError(result, logger, "must not have parameters");
    }

    @Test
    public void unqualified_gauge_function_should_be_search_for_gauge_object() {
        // Given
        TestLogger logger = new TestLogger();
        sut = new MicrometerGaugeTask(logger, GAUGE_NAME, null, null, null, null, new ArrayList<>(), "unknown", null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertOneError(result, logger, "Cannot find", "unknown", "java.util.ArrayList");
    }

    @Test
    public void gauge_function_should_return_a_number() {
        // Given
        TestLogger logger = new TestLogger();
        sut = new MicrometerGaugeTask(logger, GAUGE_NAME, null, null, null, null, new ArrayList<>(), "trimToSize", null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertOneError(result, logger, "must return a Number");
    }

    @Test
    public void gauge_object_must_be_a_collection_map_or_number_when_gauge_function_null() {
        // Given
        TestLogger logger = new TestLogger();
        sut = new MicrometerGaugeTask(logger, GAUGE_NAME, null, null, null, null, new Object(), null, null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertOneError(result, logger, "must be a Number, a Collection or a Map");
    }

    @Test
    public void should_create_gauge_from_a_number_object_when_gauge_function_null() {
        // Given
        AtomicInteger gaugeObject = new AtomicInteger(8);
        sut = new MicrometerGaugeTask(new TestLogger(), GAUGE_NAME, null, null, null, null, gaugeObject, null, null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndGaugeObjectType(result, AtomicInteger.class);

        AtomicInteger outputGauge = (AtomicInteger) result.outputs.get(OUTPUT_GAUGE);
        assertThat(outputGauge).isEqualTo(gaugeObject);

        assertGaugeValue(gaugeObject);

        // When
        gaugeObject.set(90);
        // Then
        assertGaugeValue(gaugeObject);
    }

    @Test
    public void should_create_gauge_from_a_collection_object_measuring_its_size_when_gauge_function_null() {
        // Given
        ArrayList<String> gaugeObject = new ArrayList<>();
        sut = new MicrometerGaugeTask(new TestLogger(), GAUGE_NAME, null, null, null, null, gaugeObject, null, null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndGaugeObjectType(result, List.class);

        ArrayList outputGauge = (ArrayList) result.outputs.get(OUTPUT_GAUGE);
        assertThat(outputGauge).isEqualTo(gaugeObject);

        assertGaugeValue(gaugeObject.size());

        // When
        gaugeObject.addAll(Lists.list("", "", ""));
        // Then
        assertGaugeValue(gaugeObject.size());
    }

    @Test
    public void should_create_gauge_from_a_map_object_measuring_its_size_when_gauge_function_null() {
        // Given
        HashMap<String, Object> gaugeObject = new HashMap<>();
        sut = new MicrometerGaugeTask(new TestLogger(), GAUGE_NAME, null, null, null, null, gaugeObject, null, null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndGaugeObjectType(result, Map.class);

        Map outputGauge = (Map) result.outputs.get(OUTPUT_GAUGE);
        assertThat(outputGauge).isEqualTo(gaugeObject);

        assertGaugeValue(gaugeObject.size());

        // When
        gaugeObject.putAll(Maps.of("", "", "", ""));
        // Then
        assertGaugeValue(gaugeObject.size());
    }

    @Test
    public void should_create_gauge_from_an_object_and_a_public_function_defined_on_it() {
        // Given
        class TestObject {
            private int state;

            private TestObject(int initialValue) {
                this.state = initialValue;
            }

            public double measure() {
                return 2 * state;
            }

            private void changeState(int v) {
                state /= v;
            }
        }

        TestObject gaugeObject = new TestObject(6);
        sut = new MicrometerGaugeTask(new TestLogger(), GAUGE_NAME, null, null, null, null, gaugeObject, "measure", null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndGaugeObjectType(result, TestObject.class);

        TestObject outputGauge = (TestObject) result.outputs.get(OUTPUT_GAUGE);
        assertThat(outputGauge).isEqualTo(gaugeObject);

        assertGaugeValue(gaugeObject.measure());

        // When
        gaugeObject.changeState(2);
        // Then
        assertGaugeValue(gaugeObject.measure());
    }

    @Test
    public void should_create_gauge_from_a_static_public_function() {
        // Given
        staticFunction(); // Avoid auto clean unused method
        sut = new MicrometerGaugeTask(new TestLogger(), GAUGE_NAME, null, null, null, null, null, this.getClass().getName() + ".staticFunction", null);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertThat(result.status).isEqualTo(Success);
        assertThat(result.outputs).isEmpty();

        Iterable<Measurement> firstMeasure = requireNonNull(meterRegistry.find(GAUGE_NAME).gauge()).measure();
        assertThat(firstMeasure).hasSize(1);
        double firstValue = getFirstValueFromMeasurements(firstMeasure).getValue();

        // When
        double secondValue = requireNonNull(meterRegistry.find(GAUGE_NAME).gauge()).value();
        // Then
        assertThat(requireNonNull(meterRegistry.find(GAUGE_NAME).gauge()).measure()).hasSize(1);
        assertThat(secondValue).isNotEqualTo(firstValue);
    }

    @Test
    public void should_create_gauge_and_register_it_on_given_registry() {
        // Given
        MeterRegistry givenMeterRegistry = new SimpleMeterRegistry();
        sut = new MicrometerGaugeTask(new TestLogger(), GAUGE_NAME, "description", "my unit", null, Lists.list("tag", "my tag value"), new AtomicInteger(), null, givenMeterRegistry);

        // When
        TaskExecutionResult result = sut.execute();

        // Then
        assertSuccessAndGaugeObjectType(result, AtomicInteger.class);

        assertThat(globalRegistry.find(GAUGE_NAME).gauges()).isEmpty();
        assertThat(meterRegistry.find(GAUGE_NAME).gauges()).isEmpty();
        Gauge gauge = givenMeterRegistry.find(GAUGE_NAME).gauge();
        assertThat(gauge).isNotNull();
        assertThat(gauge.getId().getDescription()).isEqualTo("description");
        assertThat(gauge.getId().getBaseUnit()).isEqualTo("my unit");
        assertThat(gauge.getId().getTag("tag")).isEqualTo("my tag value");
    }

    private void assertGaugeValue(Number gaugeObject) {
        assertThat(requireNonNull(meterRegistry.find(GAUGE_NAME).gauge()).value()).isEqualTo(gaugeObject.doubleValue());
    }

    private void assertOneError(TaskExecutionResult result, TestLogger logger, String... messages) {
        assertThat(result.status).isEqualTo(Failure);
        assertThat(logger.errors).hasSize(1);
        for (String message : messages) {
            assertThat(logger.errors.get(0)).contains(message);
        }
    }

    private void assertSuccessAndGaugeObjectType(TaskExecutionResult result, Class clazz) {
        assertSuccessAndOutputObjectType(result, OUTPUT_GAUGE, clazz);
    }

    private Measurement getFirstValueFromMeasurements(Iterable<Measurement> measurements) {
        return StreamSupport.stream(measurements.spliterator(), false)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Measurements should not be empty !!"));
    }

    public static Number staticFunction() {
        return Objects.hashCode(new Object());
    }
}
