package com.chutneytesting.task.micrometer;

import static io.micrometer.core.instrument.Metrics.globalRegistry;

import com.chutneytesting.task.spi.SpelFunction;
import io.micrometer.core.instrument.MeterRegistry;

public class MicrometerFunctions {

    @SpelFunction
    public static MeterRegistry micrometerRegistry(String registryClassName) {
        if (registryClassName == null || registryClassName.isBlank()) {
            return globalRegistry;
        }

        return globalRegistry.getRegistries().stream()
            .filter(mr -> mr.getClass().getSimpleName().contains(registryClassName))
            .findFirst()
            .orElse(globalRegistry);
    }
}
