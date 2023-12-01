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

import static com.chutneytesting.action.micrometer.MicrometerActionTestHelper.assertSuccessAndOutputObjectType;
import static com.chutneytesting.action.micrometer.MicrometerTimerStartAction.OUTPUT_TIMER_SAMPLE;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.spi.ActionExecutionResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

public class MicrometerTimerStartActionTest {

    private MicrometerTimerStartAction sut;

    @Test
    public void should_start_a_timing_sample() {
        // Given
        sut = new MicrometerTimerStartAction(new TestLogger(), null);

        // When
        ActionExecutionResult result = sut.execute();

        // Then
        assertSuccessAndSampleObjectType(result);

        Timer.Sample timerSampleOutput = (Timer.Sample) result.outputs.get(OUTPUT_TIMER_SAMPLE);
        assertThat(timerSampleOutput).isNotNull();
    }

    @Test
    public void should_start_a_timing_sample_with_a_given_registry() {
        // Given
        MeterRegistry givenMeterRegistry = new SimpleMeterRegistry();
        sut = new MicrometerTimerStartAction(new TestLogger(), givenMeterRegistry);

        // When
        ActionExecutionResult result = sut.execute();

        // Then
        assertSuccessAndSampleObjectType(result);

        Timer.Sample timerSampleOutput = (Timer.Sample) result.outputs.get(OUTPUT_TIMER_SAMPLE);
        assertThat(givenMeterRegistry.getMeters()).isEmpty();
        assertThat(timerSampleOutput).isNotNull();
    }

    @Test
    public void should_log_timing_sample_start() {
        // Given
        TestLogger logger = new TestLogger();
        sut = new MicrometerTimerStartAction(logger, null);

        // When
        ActionExecutionResult result = sut.execute();

        // Then
        assertSuccessAndSampleObjectType(result);

        assertThat(logger.info).hasSize(1);
        assertThat(logger.info.get(0)).contains("started");
    }

    private void assertSuccessAndSampleObjectType(ActionExecutionResult result) {
        assertSuccessAndOutputObjectType(result, OUTPUT_TIMER_SAMPLE, Timer.Sample.class);
    }
}
