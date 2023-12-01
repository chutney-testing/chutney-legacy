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

import static com.chutneytesting.action.micrometer.MicrometerActionHelper.doubleStringValidation;
import static com.chutneytesting.action.micrometer.MicrometerActionHelper.parseDoubleOrNull;
import static com.chutneytesting.action.micrometer.MicrometerActionHelper.toOutputs;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.action.spi.validation.Validator.of;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.validation.Validator;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;

public class MicrometerCounterAction implements Action {

    protected static final String OUTPUT_COUNTER = "micrometerCounter";

    private final Logger logger;
    private final String name;
    private final String description;
    private final String unit;
    private final List<String> tags;
    private Counter counter;
    private final MeterRegistry registry;
    private final String increment;

    public MicrometerCounterAction(Logger logger,
                                 @Input("name") String name,
                                 @Input("description") String description,
                                 @Input("unit") String unit,
                                 @Input("tags") List<String> tags,
                                 @Input("counter") Counter counter,
                                 @Input("registry") MeterRegistry registry,
                                 @Input("increment") String increment) {
        this.logger = logger;
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.tags = tags;
        this.increment = increment;
        this.counter = counter;
        this.registry = ofNullable(registry).orElse(globalRegistry);
    }

    @Override
    public List<String> validateInputs() {
        Validator<Object> metricNameValidation = of(null)
            .validate(a -> name != null || counter != null, "name and counter cannot be both null");

        return getErrorsFrom(
            metricNameValidation,
            doubleStringValidation(increment, "increment")
        );
    }

    @Override
    public ActionExecutionResult execute() {
        try {
            this.counter = ofNullable(counter).orElseGet(() -> this.retrieveCounter(registry));
            if (increment != null) {
                counter.increment(parseDoubleOrNull(increment));
                logger.info("Counter incremented by " + increment);
            }
            logger.info("Counter current count is " + counter.count());
            return ActionExecutionResult.ok(toOutputs(OUTPUT_COUNTER, counter));
        } catch (Exception e) {
            logger.error(e);
            return ActionExecutionResult.ko();
        }
    }

    private Counter retrieveCounter(MeterRegistry registry) {

        Counter.Builder builder = Counter.builder(requireNonNull(name))
            .description(description)
            .baseUnit(unit);

        ofNullable(tags).ifPresent(t -> builder.tags(t.toArray(new String[0])));

        return builder.register(registry);
    }
}
