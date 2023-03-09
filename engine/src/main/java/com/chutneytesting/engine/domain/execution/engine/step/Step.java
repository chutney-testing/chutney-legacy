package com.chutneytesting.engine.domain.execution.engine.step;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Map.entry;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.RxBus;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
import com.chutneytesting.engine.domain.execution.engine.evaluation.EvaluationException;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContext;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContextImpl;
import com.chutneytesting.engine.domain.execution.event.BeginStepExecutionEvent;
import com.chutneytesting.engine.domain.execution.event.EndStepExecutionEvent;
import com.chutneytesting.engine.domain.execution.event.PauseStepExecutionEvent;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.tools.Try;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exposes {@link #definition} and {@link #state}  of a Step.
 */
public class Step {

    private static final Logger LOGGER = LoggerFactory.getLogger(Step.class);

    private final StepDefinition definition;

    private final StepState state;
    private final List<Step> steps;
    private Target target;
    private final StepExecutor executor;
    private final StepDataEvaluator dataEvaluator;

    private StepContext stepContext;

    public Step(StepDataEvaluator dataEvaluator, StepDefinition definition, StepExecutor executor, List<Step> steps) {
        this.dataEvaluator = dataEvaluator;
        this.definition = definition;
        this.target = definition.getTarget().orElse(TargetImpl.NONE);
        this.executor = executor;
        this.steps = steps;
        this.state = new StepState();
        this.stepContext = new StepContext();
    }

    public static Step nonExecutable(StepDefinition definition) {
        return new Step(null, definition, null, emptyList()); // TODO any - Type a NonExecutableStep, or a RootStep at least
    }

    public Status execute(ScenarioExecution scenarioExecution, ScenarioContext scenarioContext) {

        if (scenarioExecution.hasToPause()) {
            final Instant startPauseInstant = Instant.now();
            pauseExecution(scenarioExecution);
            scenarioExecution.waitForRestart();
            state.resumeExecution();
            state.addInformation("Step pause from " + startPauseInstant + " to " + Instant.now());
        }

        if (scenarioExecution.hasToStop()) {
            stopExecution(scenarioExecution);
            return Status.STOPPED;
        }

        beginExecution(scenarioExecution);

        try {
            this.stepContext = new StepContext();
            this.stepContext.addLocalContext(scenarioContext);
            this.stepContext.addLocalContext(entry("target", target));
            final Map<String, Object> evaluatedInputs = definition.type.equals("final") ? definition.inputs() : unmodifiableMap(dataEvaluator.evaluateNamedDataWithContextVariables(definition.inputs(), this.stepContext.localContext));
            this.stepContext.addLocalContext(evaluatedInputs);
            target = dataEvaluator.evaluateTarget(target, this.stepContext.localContext);

            Try
                .exec(() -> this.stepContext = new StepContext(scenarioContext, this.stepContext.localContext, evaluatedInputs))
                .ifSuccess(stepContextExecuted -> {
                    executor.execute(scenarioExecution, target, this);
                    if (Status.SUCCESS.equals(this.state.status())) {
                        executeStepValidations(stepContextExecuted);
                    }
                    this.stepContext = stepContextExecuted.copy(); // todo check
                })
                .ifFailed(this::failure);
        } catch (RuntimeException e) {
            failure(e);
            LOGGER.warn("Intercepted exception!", e);
        } finally {
            endExecution(scenarioExecution);
        }
        return state.status();
    }

    public void beginExecution(ScenarioExecution scenarioExecution) {
        state.beginExecution();
        RxBus.getInstance().post(new BeginStepExecutionEvent(scenarioExecution, this));
    }

    public void endExecution(ScenarioExecution scenarioExecution) {
        state.endExecution(isParentStep());
        RxBus.getInstance().post(new EndStepExecutionEvent(scenarioExecution, this));
    }

    public void stopExecution(ScenarioExecution scenarioExecution) {
        state.addInformation("Stop requested before executing this step");
        state.stopExecution();
        RxBus.getInstance().post(new EndStepExecutionEvent(scenarioExecution, this));
    }

    public void pauseExecution(ScenarioExecution scenarioExecution) {
        state.pauseExecution();
        RxBus.getInstance().post(new PauseStepExecutionEvent(scenarioExecution, this));
    }

    public Status status() {
        if (isParentStep()) {
            final Status worstSubStepsStatus = Status.worst(subStepsStatus());
            if (Status.PAUSED.equals(worstSubStepsStatus)) {
                return Status.PAUSED;
            }
            if (Status.RUNNING.equals(state.status())) {
                return Status.RUNNING;
            }
            return worstSubStepsStatus;
        }
        return state.status();
    }

    private List<Status> subStepsStatus() {
        if (!isParentStep() || Status.FAILURE.equals(state.status())) {
            return Lists.newArrayList(state.status());
        } else {
            return this.steps.stream()
                .map(Step::status)
                .collect(Collectors.toList());
        }
    }

    public void addInformation(String... info) {
        state.addInformation(info);
    }

    public void addErrorMessage(String... errors) {
        state.addErrors(errors);
    }

    public void failure(Throwable e) {
        failure(ofNullable(e.getMessage()).orElse(e.toString()));
    }

    public void failure(String... message) {
        state.errorOccurred(message);
    }

    public void success(String... message) {
        state.successOccurred(message);
    }

    public void resetExecution() {
        state.reset();
        steps.forEach(Step::resetExecution);
    }

    public void startWatch() {
        state.startWatch();
    }

    public void stopWatch() {
        state.stopWatch();
    }

    public Duration duration() {
        return state.duration();
    }

    public Instant startDate() {
        return state.startDate();
    }

    public List<String> informations() {
        return state.informations();
    }

    public List<String> errors() {
        return state.errors();
    }

    public Target target() {
        return target;
    }

    public StepDefinition definition() {
        return definition;
    }

    public Optional<StepStrategyDefinition> strategy() {
        return definition.getStrategy();
    }

    public String type() {
        return definition.type;
    }

    public List<Step> subSteps() {
        return steps;
    }

    public boolean isParentStep() {
        return !steps.isEmpty();
    }

    public void updateFrom(ActionExecutionResult.Status status, Map<String, Object> stepOutputs) {
        updateFrom(status, stepOutputs, stepOutputs, emptyList(), emptyList());
    }

    public void updateFrom(ActionExecutionResult.Status status, Map<String, Object> stepOutputs, Map<String, Object> scenarioContext, List<String> information, List<String> errors) {
        if (status == ActionExecutionResult.Status.Success) {
            this.stepContext.addStepOutputs(stepOutputs);
            this.stepContext.addScenarioContext(scenarioContext);

            final Map<String, Object> contextAndStepResults = stepContext.allEvaluatedVariables();
            Try.exec(() -> {
                    final Map<String, Object> evaluatedOutputs = dataEvaluator.evaluateNamedDataWithContextVariables(definition.outputs, contextAndStepResults);
                    this.stepContext.addLocalContext(evaluatedOutputs);
                    this.stepContext.addStepOutputs(evaluatedOutputs);
                    this.stepContext.addScenarioContext(evaluatedOutputs); // ça oui
                    return null;
                })
                .ifFailed(e -> failure("Cannot evaluate outputs."
                    + " - Exception: " + e.getClass() + " with message: \"" + e.getMessage() + "\"")
                );

            this.success();
        } else {
            this.failure(Lists.newArrayList(errors).toArray(new String[errors.size()]));
        }

        this.addInformation(Lists.newArrayList(information).toArray(new String[information.size()]));
    }

    private void executeStepValidations(StepContext stepContext) {
        final Map<String, Object> contextAndStepResults = stepContext.allEvaluatedVariables();
        Try.exec(() -> {
                final Map<String, Object> evaluatedValidations = dataEvaluator.evaluateNamedDataWithContextVariables(definition.validations, contextAndStepResults);
                evaluatedValidations.forEach((k, v) -> {
                    if (!(boolean) v) {
                        failure("Validation [" + k + " : " + definition.validations.get(k).toString() + "] : KO");
                    } else {
                        state.addInformation("Validation [" + k + " : " + definition.validations.get(k).toString() + "] : OK");
                    }
                });
                return null;
            })
            .ifFailed(e -> failure("Step validation failed."
                + " - Exception: " + e.getClass() + " with message: \"" + e.getMessage() + "\""));
    }

    public void addStepExecution(Step step) {
        this.steps.add(step);
    }

    public Map<String, Object> getEvaluatedInputs() {
        return unmodifiableMap(this.stepContext.getEvaluatedInputs());
    }

    public Map<String, Object> getScenarioContext() {
        return this.stepContext.getScenarioContext().unmodifiable();
    }

    public Map<String, Object> getStepOutputs() {
        return unmodifiableMap(this.stepContext.getStepOutputs());
    }


    private static class StepContext {

        private final ScenarioContext scenarioContext;
        private final Map<String, Object> localContext;
        private final Map<String, Object> evaluatedInputs;
        private final Map<String, Object> stepOutputs;

        private StepContext() {
            this(new ScenarioContextImpl(), new LinkedHashMap<>(), new LinkedHashMap<>());
        }

        private StepContext(ScenarioContext scenarioContext, Map<String, Object> localContext, Map<String, Object> evaluatedInputs) throws EvaluationException {
            this(scenarioContext, localContext, evaluatedInputs, new LinkedHashMap<>());
        }

        private StepContext(ScenarioContext scenarioContext, Map<String, Object> localContext, Map<String, Object> evaluatedInputs, Map<String, Object> stepOutputs) {
            this.scenarioContext = scenarioContext;
            this.localContext = localContext;
            this.evaluatedInputs = evaluatedInputs;
            this.stepOutputs = stepOutputs;
        }

        private Map<String, Object> allEvaluatedVariables() {
            final Map<String, Object> allResults = Maps.newLinkedHashMap(scenarioContext);
            allResults.putAll(stepOutputs);
            return allResults;
        }

        private ScenarioContext getScenarioContext() {
            return scenarioContext;
        }

        private Map<String, Object> getEvaluatedInputs() {
            return ofNullable(evaluatedInputs).orElse(emptyMap());
        }

        @SafeVarargs
        private void addLocalContext(Map.Entry<String, Object>... entries) {
            this.addLocalContext(Map.ofEntries(entries));
        }

        private void addLocalContext(Map<String, Object> localContext) {
            if (localContext != null) {
                this.localContext.putAll(localContext);
            }
        }

        private void addStepOutputs(Map<String, Object> stepOutputs) {
            if (stepOutputs != null) {
                this.stepOutputs.putAll(stepOutputs);
            }
        }

        private void addScenarioContext(Map<String, Object> context) {
            if (context != null) {
                this.scenarioContext.putAll(context);
            }
        }

        private Map<String, Object> getStepOutputs() {
            return ofNullable(stepOutputs).orElse(emptyMap());
        }

        private StepContext copy() {
            return new StepContext(scenarioContext.unmodifiable(), unmodifiableMap(localContext), unmodifiableMap(evaluatedInputs), unmodifiableMap(stepOutputs));
        }
    }
}
