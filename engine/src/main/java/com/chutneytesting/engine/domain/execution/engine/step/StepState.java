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

package com.chutneytesting.engine.domain.execution.engine.step;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.domain.execution.report.Status;
import com.google.common.base.Stopwatch;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;

/**
 * Mutable part of a Step, holding data evolving during Scenario execution such as {@link Status}.
 */
public class StepState {

    private final Stopwatch stopwatch = Stopwatch.createUnstarted();

    private Status status = Status.NOT_EXECUTED;
    private Instant startDate;
    private final List<String> errors = new ArrayList<>();
    private final List<String> informations = new ArrayList<>();
    private String name;

    public StepState(String name) {
        this.name = name;
    }

    public StepState() {
    }

    void beginExecution() {
        if (!stopwatch.isRunning()) {
            stopwatch.start();
            if (isNull(startDate)) {
                startDate = Instant.now();
            }
            status = Status.RUNNING;
        }
    }

    void endExecution(boolean isParentStep) {
        if (stopwatch.isRunning()) {
            stopwatch.stop();
            if (isParentStep) {
                status = Status.EXECUTED;
            }
        }
    }

    void stopExecution() {
        status = Status.STOPPED;
    }

    void pauseExecution() {
        status = Status.PAUSED;
    }

    void resumeExecution() {
        status = Status.RUNNING;
    }

    void errorOccurred(String... message) {
        status = Status.FAILURE;
        errors.addAll(newArrayList(message));
    }

    void successOccurred(String... message) {
        status = Status.SUCCESS;
        informations.addAll(newArrayList(message));
    }

    void reset() {
        status = Status.NOT_EXECUTED;
        informations.clear();
        errors.clear();
    }

    void startWatch() {
        if (!stopwatch.isRunning()) {
            stopwatch.start();
        }
    }

    void stopWatch() {
        if (stopwatch.isRunning()) {
            stopwatch.stop();
        }
    }

    void addInformation(String... message) {
        informations.addAll(newArrayList(message));
    }

    void addErrors(String... message) {
        errors.addAll(newArrayList(message));
    }

    public Duration duration() {
        return Duration.of(stopwatch.elapsed(TimeUnit.MICROSECONDS), ChronoUnit.MICROS);
    }

    public Status status() {
        return status;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> errors() {
        return unmodifiableList(filterNullAndEmptyMessage(errors));
    }

    public List<String> informations() {
        return unmodifiableList(filterNullAndEmptyMessage(informations));
    }

    public Instant startDate() {
        return ofNullable(startDate).orElse(Instant.now());
    }

    private List<String> filterNullAndEmptyMessage(List<String> messages) {
        return newArrayList(messages).stream().filter(StringUtils::isNotEmpty).toList();
    }
}
