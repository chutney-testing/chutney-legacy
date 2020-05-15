package com.chutneytesting.engine.domain.execution.engine.step;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.RxBus;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
import com.chutneytesting.engine.domain.execution.engine.evaluation.EvaluationException;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContext;
import com.chutneytesting.engine.domain.execution.event.BeginStepExecutionEvent;
import com.chutneytesting.engine.domain.execution.event.EndStepExecutionEvent;
import com.chutneytesting.engine.domain.execution.event.PauseStepExecutionEvent;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.tools.Try;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
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
    private final Target target;
    private final StepExecutor executor;
    private final StepDataEvaluator dataEvaluator;

    private StepContextImpl stepContext;

    public Step(StepDataEvaluator dataEvaluator, StepDefinition definition, Optional<Target> target, StepExecutor executor, List<Step> steps) {
        this.dataEvaluator = dataEvaluator;
        this.definition = definition;
        this.target = target.orElse(TargetImpl.NONE);
        this.executor = executor;
        this.steps = steps;
        this.state = new StepState();
        this.stepContext = new StepContextImpl();
    }

    public static Step nonExecutable(StepDefinition definition) {
        return new Step(null, definition, Optional.empty(), null, Collections.emptyList()); // TODO any - Type a NonExecutableStep, or a RootStep at least
    }

    public Status execute(ScenarioExecution scenarioExecution, ScenarioContext scenarioContext) {

        if (scenarioExecution.hasToPause()) {
            Instant startPauseInstant = Instant.now();
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
            makeTargetAccessibleForInputEvaluation(scenarioContext, target);
            makeEnvironmentAccessibleForInputEvaluation(scenarioContext);
            Map<String, Object> evaluatedInputs = unmodifiableMap(dataEvaluator.evaluateNamedDataWithContextVariables(definition.inputs, scenarioContext));

            Try
                .exec(() -> new StepContextImpl(evaluatedInputs, scenarioContext))
                .ifSuccess(stepContextExecuted -> {
                    executor.execute(scenarioExecution, stepContextExecuted, target, this);
                    copyStepResultsToScenarioContext(stepContextExecuted, scenarioContext);
                    this.stepContext = (StepContextImpl) stepContextExecuted.copy();
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
            Status worstSubStepsStatus = Status.worst(subStepsStatus());
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

    public void failure(Exception e) {
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

    public StepContextImpl stepContext() {
        return stepContext;
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

    private void makeTargetAccessibleForInputEvaluation(ScenarioContext scenarioContext, Target target) {
        scenarioContext.put("target", target);
    }

    private void makeEnvironmentAccessibleForInputEvaluation(ScenarioContext scenarioContext) {
        scenarioContext.put("environment", definition.environment);
    }

    private void copyStepResultsToScenarioContext(StepContextImpl stepContext, ScenarioContext scenarioContext) {
        Map<String, Object> contextAndStepResults = stepContext.allEvaluatedVariables();
        Try.exec(() -> {
            scenarioContext.putAll(dataEvaluator.evaluateNamedDataWithContextVariables(definition.outputs, contextAndStepResults));
            return null;
        })
            .ifFailed(e -> failure("Cannot evaluate outputs."
                + " - Exception: " + e.getClass() + " with message: \"" + e.getMessage() + "\""));
    }

    public static class StepContextImpl implements StepContext {

        private final ScenarioContext scenarioContext;
        private final Map<String, Object> evaluatedInputs;
        private final Map<String, Object> stepOutputs;

        private StepContextImpl(Map<String, Object> evaluatedInputs, ScenarioContext scenarioContext) throws EvaluationException {
            this(scenarioContext, evaluatedInputs, new HashMap<>());
        }

        private StepContextImpl(ScenarioContext scenarioContext, Map<String, Object> evaluatedInputs, Map<String, Object> stepOutputs) {
            this.scenarioContext = scenarioContext;
            this.evaluatedInputs = evaluatedInputs;
            this.stepOutputs = stepOutputs;
        }

        private StepContextImpl() {
            this(null, emptyMap(), emptyMap());
        }

        private Map<String, Object> allEvaluatedVariables() {
            Map<String, Object> allResults = Maps.newHashMap(scenarioContext);
            allResults.putAll(stepOutputs);
            return allResults;
        }

        @Override
        public ScenarioContext getScenarioContext() {
            return scenarioContext;
        }

        @Override
        public Map<String, Object> getEvaluatedInputs() {
            return ofNullable(evaluatedInputs).orElse(emptyMap());
        }

        @Override
        public void addStepOutputs(Map<String, Object> stepOutputs) { // TODO any - clarify that it is only used for outputs evaluation
            this.stepOutputs.putAll(stepOutputs);
        }

        @Override
        public void addScenarioContext(Map<String, Object> context) {
            this.scenarioContext.putAll(context);
        }

        @Override
        public Map<String, Object> getStepOutputs() { // TODO any - clarify that it is only used for outputs evaluation
            return unmodifiableMap(ofNullable(stepOutputs).orElse(emptyMap()));
        }

        StepContext copy() {
            return new StepContextImpl(scenarioContext.unmodifiable(), unmodifiableMap(evaluatedInputs), unmodifiableMap(stepOutputs));
        }
    }
}
