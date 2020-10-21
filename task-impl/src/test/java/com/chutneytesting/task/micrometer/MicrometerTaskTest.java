package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.spi.TaskExecutionResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.After;
import org.junit.Before;

abstract class MicrometerTaskTest {

    MeterRegistry meterRegistry;

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

    void assertSuccessAndOutputObjectType(TaskExecutionResult result, String outputKey, Class clazz) {
        assertThat(result.status).isEqualTo(Success);
        assertThat(result.outputs).containsOnlyKeys(outputKey);
        assertThat(result.outputs)
            .extractingByKey(outputKey)
            .isInstanceOf(clazz);
    }
}
