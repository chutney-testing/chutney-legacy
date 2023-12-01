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

import static com.chutneytesting.action.micrometer.MicrometerActionHelper.toOutputs;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class MicrometerTimerStartAction implements Action {

    protected static final String OUTPUT_TIMER_SAMPLE = "micrometerTimerSample";

    private final Logger logger;
    private final MeterRegistry registry;

    public MicrometerTimerStartAction(Logger logger,
                                    @Input("registry") MeterRegistry registry) {
        this.logger = logger;
        this.registry = ofNullable(registry).orElse(globalRegistry);
    }

    @Override
    public ActionExecutionResult execute() {
        try {
            Timer.Sample sample = Timer.start(registry);
            logger.info("Timing sample started");
            return ActionExecutionResult.ok(toOutputs(OUTPUT_TIMER_SAMPLE, sample));
        } catch (Exception e) {
            logger.error(e);
            return ActionExecutionResult.ko();
        }
    }
}
