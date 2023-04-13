package com.chutneytesting.engine.domain.execution.engine;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.engine.domain.delegation.DelegationService;
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
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StrategyProperties;
import com.chutneytesting.engine.domain.report.Reporter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultExecutionEngine implements ExecutionEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExecutionEngine.class);

    private final Executor actionExecutor;

    private final StepDataEvaluator dataEvaluator;
    private final StepExecutionStrategies stepExecutionStrategies;
    private final DelegationService delegationService;
    private final Reporter reporter;

    public DefaultExecutionEngine(StepDataEvaluator dataEvaluator,
                                  StepExecutionStrategies stepExecutionStrategies,
                                  DelegationService delegationService,
                                  Reporter reporter,
                                  Executor actionExecutor) {
        this.dataEvaluator = dataEvaluator;
        this.stepExecutionStrategies = stepExecutionStrategies != null ? stepExecutionStrategies : new StepExecutionStrategies();
        this.delegationService = delegationService;
        this.reporter = reporter;
        this.actionExecutor = actionExecutor;
    }

    @Override
    public Long execute(StepDefinition stepDefinition, Dataset dataset, ScenarioExecution execution) {

        AtomicReference<Step> rootStep = new AtomicReference<>(Step.nonExecutable(stepDefinition));
        reporter.createPublisher(execution.executionId, rootStep.get());

        actionExecutor.execute(() -> {

            final ScenarioContext scenarioContext = new ScenarioContextImpl();
            scenarioContext.put("environment", stepDefinition.environment);
            scenarioContext.put("dataset", dataset.datatable);
            scenarioContext.putAll(dataset.constants);

            try {
                try {
                    rootStep.set(buildStep(stepDefinition));
                    RxBus.getInstance().post(new StartScenarioExecutionEvent(execution, rootStep.get()));

                    final StepExecutionStrategy strategy = stepExecutionStrategies.buildStrategyFrom(rootStep.get());
                    strategy.execute(execution, rootStep.get(), scenarioContext, stepExecutionStrategies);
                } catch (VirtualMachineError vme) {
                    throw vme; // OutOfMemoryError, StackOverflowError, InternalError, UnknownError
                } catch (Throwable t) {
                    // Do not remove this fault barrier, the engine must not be stopped by external events
                    // (such as exceptions not raised by the engine)
                    rootStep.get().failure(t);
                    LOGGER.warn("Intercepted exception in root step execution !", t);
                }

                executeFinallyActions(execution, rootStep, scenarioContext);

            } finally {
                RxBus.getInstance().post(new EndScenarioExecutionEvent(execution, rootStep.get()));
            }
        });

        return execution.executionId;
    }

    private Optional<Step> initFinalRootStep(AtomicReference<Step> rootStep, List<FinallyAction> finallyActionsSnapshot) {
        try {
            String environment = rootStep.get().definition().environment;
            Pair<List<StepDefinition>, List<Step>> finalStepsWithDefinitions = finallyActionsSnapshot.stream()
                .map(fa -> {
                    StepDefinition definition = new FinallyActionMapper().toStepDefinition(fa, environment);
                    return Pair.of(singletonList(definition), singletonList(buildStep(definition)));
                })
                .reduce(Pair.of(new ArrayList<>(), new ArrayList<>()), (p1, p2) -> {
                    p1.getLeft().addAll(p2.getLeft());
                    p1.getRight().addAll(p2.getRight());
                    return p1;
                });

            StepDefinition finalRootStepDefinition = new StepDefinition(
                "TearDown",
                null,
                "",
                new StepStrategyDefinition("soft-assert", new StrategyProperties()),
                emptyMap(),
                finalStepsWithDefinitions.getLeft(),
                emptyMap(),
                emptyMap(),
                environment
            );

            return Optional.of(
                new Step(dataEvaluator, finalRootStepDefinition, delegationService.findExecutor(empty()), finalStepsWithDefinitions.getRight())
            );
        } catch (RuntimeException e) {
            rootStep.get().failure(e);
            LOGGER.warn("Cannot init final root step !", e);
            return empty();
        }
    }

    private void executeFinallyActions(ScenarioExecution execution, AtomicReference<Step> rootStep, ScenarioContext scenarioContext) {
        if (!execution.finallyActions().isEmpty()) {
            List<FinallyAction> finallyActionsSnapshot = new ArrayList<>(execution.finallyActions());

            Optional<Step> finalRootStep = initFinalRootStep(rootStep, finallyActionsSnapshot);
            finalRootStep.ifPresent(frs -> {
                rootStep.get().addStepExecution(frs);
                execution.initFinallyActionExecution();

                try {
                    final StepExecutionStrategy strategy = stepExecutionStrategies.buildStrategyFrom(frs);
                    strategy.execute(execution, frs, scenarioContext, stepExecutionStrategies);
                } catch (VirtualMachineError vme) {
                    throw vme;
                } catch (Throwable t) {
                    frs.failure(t);
                    LOGGER.warn("Teardown did not finish properly !", t);
                }
            });
        }
    }

    private Step buildStep(StepDefinition definition) {
        LOGGER.debug("Build : " + definition);
        final Optional<Target> target = definition.getTarget();
        final StepExecutor executor = delegationService.findExecutor(target);
        final List<Step> steps = definition.steps.stream().map(this::buildStep).collect(toList());

        return new Step(dataEvaluator, definition, executor, steps);
    }
}
