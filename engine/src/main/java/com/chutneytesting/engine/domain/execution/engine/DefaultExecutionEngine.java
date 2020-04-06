package com.chutneytesting.engine.domain.execution.engine;

import com.chutneytesting.engine.domain.delegation.DelegationService;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.ExecutionEngine;
import com.chutneytesting.engine.domain.execution.RxBus;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContext;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContextImpl;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.event.EndScenarioExecutionEvent;
import com.chutneytesting.engine.domain.execution.event.StartScenarioExecutionEvent;
import com.chutneytesting.engine.domain.execution.strategies.StepExecutionStrategies;
import com.chutneytesting.engine.domain.execution.strategies.StepExecutionStrategy;
import com.chutneytesting.engine.domain.report.Reporter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultExecutionEngine implements ExecutionEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExecutionEngine.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final StepDataEvaluator dataEvaluator;
    private final StepExecutionStrategies stepExecutionStrategies;
    private final DelegationService delegationService;
    private final Reporter reporter;

    public DefaultExecutionEngine(StepDataEvaluator dataEvaluator,
                                  StepExecutionStrategies stepExecutionStrategies,
                                  DelegationService delegationService,
                                  Reporter reporter) {
        this.dataEvaluator = dataEvaluator;
        this.stepExecutionStrategies = stepExecutionStrategies != null ? stepExecutionStrategies : new StepExecutionStrategies();
        this.delegationService = delegationService;
        this.reporter = reporter;
    }

    @Override
    public Long execute(StepDefinition stepDefinition, ScenarioExecution execution) {

        AtomicReference<Step> rootStep = new AtomicReference<>(Step.nonExecutable(stepDefinition));
        reporter.createPublisher(execution.executionId, rootStep.get());

        executorService.submit(() -> {

            final ScenarioContext scenarioContext = new ScenarioContextImpl();

            try {
                rootStep.set(buildStep(stepDefinition));

                RxBus.getInstance().post(new StartScenarioExecutionEvent(execution, rootStep.get()));

                final StepExecutionStrategy strategy = stepExecutionStrategies.buildStrategyFrom(rootStep.get());
                strategy.execute(execution, rootStep.get(), scenarioContext, stepExecutionStrategies);
            } catch (RuntimeException e) {
                // Do not remove this fault barrier, the engine must not be stopped by external events
                // (such as exceptions not raised by the engine)
                rootStep.get().failure(e);
                LOGGER.warn("Intercepted exception!", e);
            }

            try {
                execution.executeFinallyActions(
                    scenarioContext,
                    finallyAction -> buildStep(new FinallyActionMapper().toStepDefinition(finallyAction))
                );
            } catch (RuntimeException e) {
                // Do not remove this fault barrier, the engine must not be stopped by external events
                // (such as exceptions not raised by the engine)
                rootStep.get().failure(e);
                LOGGER.warn("Teardown did not finish properly !", e);
            }

            RxBus.getInstance().post(new EndScenarioExecutionEvent(execution, rootStep.get()));
        });

        return execution.executionId;
    }

    private Step buildStep(StepDefinition definition) {
        LOGGER.debug("Build : " + definition);
        final Optional<TargetImpl> target = definition.getTarget();
        final StepExecutor executor = delegationService.findExecutor(target);
        final List<Step> steps = Collections.unmodifiableList(definition.steps.stream().map(this::buildStep).collect(Collectors.toList()));

        return new Step(dataEvaluator, definition, target, executor, steps);
    }
}
