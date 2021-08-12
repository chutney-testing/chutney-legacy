package com.chutneytesting.task.micrometer;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.spi.TaskExecutionResult;
import java.util.Random;

final class MicrometerTaskTestHelper {

    private static final Random rand = new Random();

    static void assertSuccessAndOutputObjectType(TaskExecutionResult result, String outputKey, Class clazz) {
        assertThat(result.status).isEqualTo(Success);
        assertThat(result.outputs).containsOnlyKeys(outputKey);
        assertThat(result.outputs)
            .extractingByKey(outputKey)
            .isInstanceOf(clazz);
    }

    static String buildMeterName(String prefix) {
        return prefix + "_" + rand.nextInt(10000);
    }
}
