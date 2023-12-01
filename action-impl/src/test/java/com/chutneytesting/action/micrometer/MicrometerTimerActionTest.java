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
import static com.chutneytesting.action.micrometer.MicrometerActionTestHelper.buildMeterName;
import static com.chutneytesting.action.micrometer.MicrometerTimerAction.OUTPUT_TIMER;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.spi.ActionExecutionResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

public class MicrometerTimerActionTest {

    private MicrometerTimerAction sut;
    private final String METER_NAME_PREFIX = "timerName";

    @Test
    public void summary_name_is_mandatory_if_no_given_summary() {
        MicrometerTimerAction micrometerTimerAction = new MicrometerTimerAction(null, null, null, null, "not a integer", "not a duration", "not a duration", "not a duration", "not a integer", null, "not a list of double", "not a list of duration", null, null, null, "not a duration");
        List<String> errors = micrometerTimerAction.validateInputs();

        assertThat(errors.size()).isEqualTo(9);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(errors.get(0)).isEqualTo("name and timer cannot be both null");
        softly.assertThat(errors.get(1)).isEqualTo("[bufferLength parsing] not applied because of exception java.lang.NumberFormatException(For input string: \"not a integer\")");
        softly.assertThat(errors.get(2)).isEqualTo("[percentilePrecision parsing] not applied because of exception java.lang.NumberFormatException(For input string: \"not a integer\")");
        softly.assertThat(errors.get(3)).startsWith("[maxValue is not parsable] not applied because of exception java.lang.IllegalArgumentException(Cannot parse duration: not a duration");
        softly.assertThat(errors.get(4)).startsWith("[minValue is not parsable] not applied because of exception java.lang.IllegalArgumentException(Cannot parse duration: not a duration");
        softly.assertThat(errors.get(5)).startsWith("[record is not parsable] not applied because of exception java.lang.IllegalArgumentException(Cannot parse duration: not a duration");
        softly.assertThat(errors.get(6)).startsWith("[expiry is not parsable] not applied because of exception java.lang.IllegalArgumentException(Cannot parse duration: not a duration");
        softly.assertThat(errors.get(7)).isEqualTo("[Cannot parse percentils list] not applied because of exception java.lang.NumberFormatException(For input string: \"not a list of double\")");
        softly.assertThat(errors.get(8)).isEqualTo("[Cannot parse sla list] not applied because of exception java.lang.NumberFormatException(For input string: \"not a list of duration\")");
        softly.assertAll();
    }

    @Test
    public void should_create_micrometer_timer() {
        // Given
        MeterRegistry registry = new SimpleMeterRegistry();
        String meterName = buildMeterName(METER_NAME_PREFIX);
        sut = new MicrometerTimerAction(new TestLogger(), meterName, null, null, null, null, null, null, null, null, null, null, null, registry, null, null);

        // When
        ActionExecutionResult result = sut.execute();

        // Then
        assertSuccessAndTimerObjectType(result);

        Timer outputTimer = (Timer) result.outputs.get(OUTPUT_TIMER);
        assertThat(registry.find(meterName).timer()).isEqualTo(outputTimer);
        assertThat(outputTimer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(0);
        assertThat(outputTimer.max(TimeUnit.MILLISECONDS)).isEqualTo(0);
        assertThat(outputTimer.mean(TimeUnit.MILLISECONDS)).isEqualTo(0);
        assertThat(outputTimer.count()).isEqualTo(0);
    }

    @Test
    public void should_create_micrometer_counter_and_register_it_on_given_registry() {
        // Given
        MeterRegistry givenMeterRegistry = new SimpleMeterRegistry();
        String meterName = buildMeterName(METER_NAME_PREFIX);
        sut = new MicrometerTimerAction(new TestLogger(), meterName, "description", Lists.list("tag", "my tag value"), null, null, null, null, null, null, null, null, null, givenMeterRegistry, null, null);

        // When
        ActionExecutionResult result = sut.execute();

        // Then
        assertSuccessAndTimerObjectType(result);

        Timer outputTimer = (Timer) result.outputs.get(OUTPUT_TIMER);
        assertThat(globalRegistry.find(meterName).timers()).isEmpty();
        assertThat(givenMeterRegistry.find(meterName).timer()).isEqualTo(outputTimer);
        assertThat(outputTimer.getId().getDescription()).isEqualTo("description");
        assertThat(outputTimer.getId().getTag("tag")).isEqualTo("my tag value");
    }

    @Test
    public void should_create_micrometer_timer_and_record_an_event() {
        // Given
        sut = new MicrometerTimerAction(new TestLogger(), buildMeterName(METER_NAME_PREFIX), null, null, null, null, null, null, null, null, null, null, null, new SimpleMeterRegistry(), null, "3 s");

        // When
        ActionExecutionResult result = sut.execute();

        // Then
        assertSuccessAndTimerObjectType(result);

        Timer outputTimer = (Timer) result.outputs.get(OUTPUT_TIMER);
        assertThat(outputTimer.totalTime(TimeUnit.SECONDS)).isEqualTo(3);
        assertThat(outputTimer.max(TimeUnit.SECONDS)).isEqualTo(3);
        assertThat(outputTimer.mean(TimeUnit.SECONDS)).isEqualTo(3);
        assertThat(outputTimer.count()).isEqualTo(1);
    }

    @Test
    public void should_record_an_event_with_given_timer() {
        // Given
        MeterRegistry registry = new SimpleMeterRegistry();
        Timer givenTimer = registry.timer(buildMeterName(METER_NAME_PREFIX));
        givenTimer.record(3, TimeUnit.SECONDS);
        sut = new MicrometerTimerAction(new TestLogger(), null, null, null, null, null, null, null, null, null, null, null, givenTimer, registry, null, "6 s");

        // When
        ActionExecutionResult result = sut.execute();

        // Then
        assertSuccessAndTimerObjectType(result);

        Timer outputTimer = (Timer) result.outputs.get(OUTPUT_TIMER);
        assertThat(outputTimer).isEqualTo(givenTimer);
        assertThat(outputTimer.totalTime(TimeUnit.SECONDS)).isEqualTo(9);
        assertThat(outputTimer.max(TimeUnit.SECONDS)).isEqualTo(6);
        assertThat(outputTimer.mean(TimeUnit.SECONDS)).isEqualTo(4.5);
        assertThat(outputTimer.count()).isEqualTo(2);
    }

    @Test
    public void should_log_timer_record_total_max_mean_and_count() {
        // Given
        TestLogger logger = new TestLogger();
        sut = new MicrometerTimerAction(logger, buildMeterName(METER_NAME_PREFIX), null, null, null, null, null, null, null, null, null, null, null, new SimpleMeterRegistry(), null, "6 s");

        // When
        ActionExecutionResult result = sut.execute();

        // Then
        assertSuccessAndTimerObjectType(result);

        assertThat(logger.info).hasSize(5);
        assertThat(logger.info.get(0)).isEqualTo("Timer updated by 6 s");
        assertThat(logger.info.get(1)).isEqualTo("Timer current total time is 6.0 SECONDS");
        assertThat(logger.info.get(2)).isEqualTo("Timer current max time is 6.0 SECONDS");
        assertThat(logger.info.get(3)).isEqualTo("Timer current mean time is 6.0 SECONDS");
        assertThat(logger.info.get(4)).isEqualTo("Timer current count is 1");
    }

    private void assertSuccessAndTimerObjectType(ActionExecutionResult result) {
        assertSuccessAndOutputObjectType(result, OUTPUT_TIMER, Timer.class);
    }
}
