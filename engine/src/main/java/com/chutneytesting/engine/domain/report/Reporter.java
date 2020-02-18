package com.chutneytesting.engine.domain.report;


import com.chutneytesting.engine.domain.execution.RxBus;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.event.BeginStepExecutionEvent;
import com.chutneytesting.engine.domain.execution.event.EndScenarioExecutionEvent;
import com.chutneytesting.engine.domain.execution.event.EndStepExecutionEvent;
import com.chutneytesting.engine.domain.execution.event.Event;
import com.chutneytesting.engine.domain.execution.event.PauseStepExecutionEvent;
import com.chutneytesting.engine.domain.execution.event.StartScenarioExecutionEvent;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReportBuilder;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Reporter.class);
    private static final long DEFAULT_RETENTION_DELAY_SECONDS = 5;

    private Map<Long, Subject<StepExecutionReport>> reportsPublishers = new ConcurrentHashMap<>();
    private Map<Long, Step> rootSteps = new ConcurrentHashMap<>();
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
        doIfPublisherExists(event.executionId(), (observer) -> observer.onNext(generateReport(event.executionId())));
    }

    private void publishReportAndCompletePublisher(Event event) {
        doIfPublisherExists(event.executionId(), (observer) -> {
                publishReport(event);
                completePublisher(event.executionId(), observer);
            });
    }

    private StepExecutionReport generateReport(long executionId) {
        return generateReport(rootSteps.get(executionId));
    }

    StepExecutionReport generateReport(Step step) {
        Step.StepContextImpl stepContext = step.stepContext();
        return new StepExecutionReportBuilder().setName(step.definition().name)
            .setDuration(step.duration().toMillis())
            .setStartDate(step.startDate())
            .setStatus(step.status())
            .setInformation(step.informations())
            .setErrors(step.errors())
            .setSteps(step.subSteps().stream().map(this::generateReport).collect(Collectors.toList()))
            .setEvaluatedInputs(stepContext.getEvaluatedInputs())
            .setStepResults(stepContext.getStepOutputs())
            .setScenarioContext(stepContext.getScenarioContext())
            .setType(step.type())
            .setTarget(step.target())
            .setStrategy(guardNullStrategy(step.strategy()))
            .createStepExecutionReport();
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
