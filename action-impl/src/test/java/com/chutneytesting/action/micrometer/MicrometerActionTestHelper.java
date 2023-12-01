/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
