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

import static com.chutneytesting.action.micrometer.MicrometerActionHelper.logTimerState;
import static com.chutneytesting.action.micrometer.MicrometerActionHelper.toOutputs;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.action.spi.validation.Validator.of;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.valueOf;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.validation.Validator;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MicrometerTimerStopAction implements Action {

    protected static final String OUTPUT_TIMER_SAMPLE_DURATION = "micrometerTimerSampleDuration";

    private final Logger logger;
    private final Timer.Sample sample;
    private final Timer timer;
    private final String timeunit;

    public MicrometerTimerStopAction(Logger logger,
                                   @Input("sample") Timer.Sample sample,
                                   @Input("timer") Timer timer,
                                   @Input("timeunit") String timeunit) {
        this.logger = logger;
        this.sample = sample;
        this.timer = timer;
        this.timeunit = ofNullable(timeunit).orElse(TimeUnit.SECONDS.name());
    }

    @Override
    public List<String> validateInputs() {
        Validator<Timer.Sample> sampleValidation = of(sample)
            .validate(Objects::nonNull, "No sample provided");
        Validator<Timer> timerValidation = of(timer)
            .validate(Objects::nonNull, "No timer provided");
        Validator<String> timeunitValidation = of(timeunit)
            .validate(t -> valueOf(timeunit), noException -> true, "Timeunit not parsable");
        return getErrorsFrom(sampleValidation, timerValidation, timeunitValidation);
    }

    @Override
    public ActionExecutionResult execute() {
        try {
            long duration = sample.stop(timer);
            Duration durationObj = Duration.of(duration, ChronoUnit.NANOS);
            logger.info("Timer sample stopped and last for " + durationObj);
            logTimerState(logger, timer, valueOf(timeunit));
            return ActionExecutionResult.ok(toOutputs(OUTPUT_TIMER_SAMPLE_DURATION, durationObj));
        } catch (Exception e) {
            logger.error(e);
            return ActionExecutionResult.ko();
        }
    }
}
