package com.chutneytesting.action.micrometer;

import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.spi.ActionExecutionResult;
import java.util.Random;

final class MicrometerActionTestHelper {

    private static final Random rand = new Random();

    static void assertSuccessAndOutputObjectType(ActionExecutionResult result, String outputKey, Class clazz) {
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
