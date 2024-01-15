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

package com.chutneytesting.engine.domain.report;


import static com.chutneytesting.engine.domain.execution.report.Status.PAUSED;
import static com.chutneytesting.engine.domain.execution.report.Status.RUNNING;

import com.chutneytesting.engine.domain.execution.RxBus;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.event.BeginStepExecutionEvent;
import com.chutneytesting.engine.domain.execution.event.EndScenarioExecutionEvent;
import com.chutneytesting.engine.domain.execution.event.EndStepExecutionEvent;
import com.chutneytesting.engine.domain.execution.event.Event;
import com.chutneytesting.engine.domain.execution.event.PauseStepExecutionEvent;
import com.chutneytesting.engine.domain.execution.event.StartScenarioExecutionEvent;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReportBuilder;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Reporter.class);
    private static final long DEFAULT_RETENTION_DELAY_SECONDS = 5;

    private final Map<Long, Subject<StepExecutionReport>> reportsPublishers = new ConcurrentHashMap<>();
    private final Map<Long, Step> rootSteps = new ConcurrentHashMap<>();
    private long retentionDelaySeconds;

    public Reporter() {
        this(DEFAULT_RETENTION_DELAY_SECONDS);
    }

    public Reporter(long retentionDelaySeconds) {
        this.retentionDelaySeconds = retentionDelaySeconds;
        busRegistration();
    }

    public Observable<StepExecutionReport> subscribeOnExecution(Long executionId) {
        LOGGER.trace("Subscribe for execution {}", executionId);
        return Optional.ofNullable((Observable<StepExecutionReport>) reportsPublishers.get(executionId))
            .orElseGet(Observable::empty);
    }

    public void setRetentionDelaySeconds(long retentionDelaySeconds) {
        this.retentionDelaySeconds = retentionDelaySeconds;
    }

    public void createPublisher(Long executionId, Step rootStep) {
        LOGGER.trace("Create publisher for execution {}", executionId);
        reportsPublishers.put(executionId, ReplaySubject.<StepExecutionReport>createWithSize(1).toSerialized());
        rootSteps.put(executionId, rootStep);
        LOGGER.debug("Publishers map size : {}", reportsPublishers.size());
    }

    private void storeRootStepAndPublishReport(StartScenarioExecutionEvent event) {
        LOGGER.trace("Store root step for execution {}", event.executionId());
        rootSteps.put(event.executionId(), event.step);
        publishReport(event);
    }

    private void publishReport(Event event) {
        LOGGER.trace("Publish report for execution {}", event.executionId());
        doIfPublisherExists(event.executionId(), (observer) -> observer.onNext(generateRunningReport(event.executionId())));
    }

    private void publishLastReport(Event event) {
        LOGGER.trace("Publish report for execution {}", event.executionId());
        doIfPublisherExists(event.executionId(), (observer) -> observer.onNext(generateLastReport(event.executionId())));
    }

    private void publishReportAndCompletePublisher(Event event) {
        doIfPublisherExists(event.executionId(), (observer) -> {
            publishLastReport(event);
            completePublisher(event.executionId(), observer);
        });
    }

    private StepExecutionReport generateRunningReport(long executionId) {
        Step step = rootSteps.get(executionId);
        final Status calculatedRootStepStatus = step.status();

        final Status finalStatus;
        if (!calculatedRootStepStatus.equals(RUNNING) && !calculatedRootStepStatus.equals(PAUSED)) {
            finalStatus = RUNNING;
        } else {
            finalStatus = calculatedRootStepStatus;
        }
        return generateReport(step, s -> finalStatus, (String) step.getScenarioContext().get("environment"));
    }

    private StepExecutionReport generateLastReport(long executionId) {
        Step step = rootSteps.get(executionId);
        return generateReport(step, Step::status, (String) step.getScenarioContext().get("environment"));
    }

    StepExecutionReport generateReport(Step step, Function<Step, Status> statusSupplier, String env) {
        try {
            return new StepExecutionReportBuilder()
                .setName(step.name())
                .setEnvironment(env)
                .setDuration(step.duration().toMillis())
                .setStartDate(step.startDate())
                .setStatus(statusSupplier.apply(step))
                .setInformation(step.informations())
                .setErrors(step.errors())
                .setSteps(step.subSteps().stream().map(subStep -> generateReport(subStep, Step::status, env)).collect(Collectors.toList()))
                .setEvaluatedInputs(step.getEvaluatedInputs())
                .setStepResults(step.getStepOutputs())
                .setScenarioContext(step.getScenarioContext())
                .setType(step.type())
                .setTarget(step.target())
                .setStrategy(guardNullStrategy(step.strategy()))
                .createStepExecutionReport();
        } catch (Exception e) {
            String error = "Cannot generate step report: " + e.getMessage();
            LOGGER.error(error, e);
            return new StepExecutionReportBuilder()
                .setName(step.name())
                .setStatus(Status.FAILURE)
                .setErrors(List.of(error))
                .createStepExecutionReport();
        }
    }

    /* TODO mbb - hack - remove me when core module domain is decouple from lite-engine domain & API */
    private String guardNullStrategy(Optional<StepStrategyDefinition> strategy) {
        return strategy.map(stepStrategyDefinition -> stepStrategyDefinition.type).orElse(null);
    }

    private void completePublisher(long executionId, Observer<StepExecutionReport> observer) {
        LOGGER.trace("Complete publisher for execution {}", executionId);
        observer.onComplete();
        if (retentionDelaySeconds > 0) {
            Completable.timer(retentionDelaySeconds, TimeUnit.SECONDS)
                .subscribe(() -> {
                    rootSteps.remove(executionId);
                    reportsPublishers.remove(executionId);
                    LOGGER.trace("Remove publisher for execution {}", executionId);
                }, throwable -> LOGGER.error("Cannot remove publisher for execution {}", executionId, throwable));
        } else {
            rootSteps.remove(executionId);
            reportsPublishers.remove(executionId);
        }
    }

    private void doIfPublisherExists(long executionId, Consumer<Observer<StepExecutionReport>> consumer) {
        Optional.ofNullable((Observer<StepExecutionReport>) reportsPublishers.get(executionId))
            .ifPresent(consumer);
    }

    private void busRegistration() {
        RxBus bus = RxBus.getInstance();
        bus.register(StartScenarioExecutionEvent.class, this::storeRootStepAndPublishReport);
        bus.register(BeginStepExecutionEvent.class, this::publishReport);
        bus.register(EndStepExecutionEvent.class, this::publishReport);
        bus.register(PauseStepExecutionEvent.class, this::publishReport);
        bus.register(EndScenarioExecutionEvent.class, this::publishReportAndCompletePublisher);
    }
}
