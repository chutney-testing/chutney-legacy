package com.chutneytesting.engine.domain.execution.engine.step;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.chutneytesting.engine.domain.execution.report.Status;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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

    void beginExecution() {
        if(!stopwatch.isRunning()) {
            stopwatch.start();
            if (Objects.isNull(startDate)) {
                startDate = Instant.now();
            }
            status = Status.RUNNING;
        }
    }

    void endExecution(boolean isParentStep) {
        if(stopwatch.isRunning()) {
            stopwatch.stop();
            if(isParentStep) {
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
        errors.addAll(Arrays.stream(message).filter(StringUtils::isNotEmpty).collect(Collectors.toList()));
    }

    void successOccurred(String... message) {
        status = Status.SUCCESS;
        informations.addAll(Lists.newArrayList(message));
    }

    void reset() {
        status = Status.NOT_EXECUTED;
        informations.clear();
        errors.clear();
    }

    void startWatch() {
        if(!stopwatch.isRunning()) {
            stopwatch.start();
        }
    }

    void stopWatch() {
        if(stopwatch.isRunning()) {
            stopwatch.stop();
        }
    }

    void addInformation(String... message) {
        informations.addAll(Lists.newArrayList(message));
    }

    void addErrors(String... message) {
        errors.addAll(Lists.newArrayList(message));
    }

    public Duration duration() {
        return stopwatch.elapsed();
    }

    public Status status() {
        return status;
    }

    public List<String> errors() {
        return Collections.unmodifiableList(errors);
    }

    public List<String> informations() {
        return Collections.unmodifiableList(informations);
    }

    public Instant startDate() {
        return Optional.ofNullable(startDate).orElse(Instant.now());
    }


}
