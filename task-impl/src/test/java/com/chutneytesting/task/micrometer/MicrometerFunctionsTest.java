package com.chutneytesting.task.micrometer;

import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.pause.PauseDetector;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class MicrometerFunctionsTest {

    @Test
    public void should_retrieve_micrometer_monitoring_system_registry_by_class_name() {
        // Given
        MeterRegistry monitoringSystemRegistry = new CustomMeterRegistry();
        globalRegistry.add(monitoringSystemRegistry);

        // When
        MeterRegistry result = MicrometerFunctions.micrometerRegistry("CustomMeterRegistry");

        // Then
        assertThat(result).isEqualTo(monitoringSystemRegistry);
    }

    @Test
    public void should_retrieve_micrometer_global_registry_when_no_one_found() {
        // Given
        SimpleMeterRegistry monitoringSystemRegistry = new SimpleMeterRegistry();
        globalRegistry.add(monitoringSystemRegistry);

        // When
        MeterRegistry result = MicrometerFunctions.micrometerRegistry("unknownRegistryClassName");

        // Then
        assertThat(result).isEqualTo(globalRegistry);
    }

    @ParameterizedTest()
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    public void should_retrieve_micrometer_global_registry_when_blank(String registryClassName) {
        // Given
        MeterRegistry monitoringCustomRegistry = new CustomMeterRegistry();
        SimpleMeterRegistry monitoringSystemRegistry = new SimpleMeterRegistry();
        globalRegistry.add(monitoringCustomRegistry);
        globalRegistry.add(monitoringSystemRegistry);

        // When
        MeterRegistry result = MicrometerFunctions.micrometerRegistry(registryClassName);

        // Then
        assertThat(result).isEqualTo(globalRegistry);
    }

    private static class CustomMeterRegistry extends MeterRegistry {

        public CustomMeterRegistry() {
            super(Clock.SYSTEM);
        }

        @Override
        protected <T> Gauge newGauge(Meter.Id id, T obj, ToDoubleFunction<T> valueFunction) {
            return null;
        }

        @Override
        protected Counter newCounter(Meter.Id id) {
            return null;
        }

        @Override
        protected Timer newTimer(Meter.Id id, DistributionStatisticConfig distributionStatisticConfig, PauseDetector pauseDetector) {
            return null;
        }

        @Override
        protected DistributionSummary newDistributionSummary(Meter.Id id, DistributionStatisticConfig distributionStatisticConfig, double scale) {
            return null;
        }

        @Override
        protected Meter newMeter(Meter.Id id, Meter.Type type, Iterable<Measurement> measurements) {
            return null;
        }

        @Override
        protected <T> FunctionTimer newFunctionTimer(Meter.Id id, T obj, ToLongFunction<T> countFunction, ToDoubleFunction<T> totalTimeFunction, TimeUnit totalTimeFunctionUnit) {
            return null;
        }

        @Override
        protected <T> FunctionCounter newFunctionCounter(Meter.Id id, T obj, ToDoubleFunction<T> countFunction) {
            return null;
        }

        @Override
        protected TimeUnit getBaseTimeUnit() {
            return null;
        }

        @Override
        protected DistributionStatisticConfig defaultHistogramConfig() {
            return null;
        }
    }
}
