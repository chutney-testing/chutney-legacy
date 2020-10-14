package com.chutneytesting.task.micrometer;

import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Test;

public class MicrometerFunctionsTest {

    @Test
    public void should_retrieve_micrometer_monitoring_system_registry_by_class_name() {
        // Given
        SimpleMeterRegistry monitoringSystemRegistry = new SimpleMeterRegistry();
        globalRegistry.add(monitoringSystemRegistry);

        // When
        MeterRegistry result = MicrometerFunctions.micrometerRegistry("SimpleMeter");

        // Then
        assertThat(result).isEqualTo(monitoringSystemRegistry);
    }

    @Test
    public void should_retrieve_micormeter_global_registry_when_no_one_found() {
        // Given
        SimpleMeterRegistry monitoringSystemRegistry = new SimpleMeterRegistry();
        globalRegistry.add(monitoringSystemRegistry);

        // When
        MeterRegistry result = MicrometerFunctions.micrometerRegistry("unknownRegistryClassName");

        // Then
        assertThat(result).isEqualTo(globalRegistry);
    }
}
