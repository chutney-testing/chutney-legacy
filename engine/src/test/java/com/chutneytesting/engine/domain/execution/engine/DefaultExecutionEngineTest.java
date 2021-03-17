package com.chutneytesting.engine.domain.execution.engine;

import static com.chutneytesting.engine.domain.execution.RxBus.getInstance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.domain.delegation.DelegationService;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.action.StopExecutionAction;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.event.BeginStepExecutionEvent;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.chutneytesting.engine.domain.execution.strategies.StepExecutionStrategies;
import com.chutneytesting.engine.domain.execution.strategies.StepExecutionStrategy;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StrategyProperties;
import com.chutneytesting.engine.domain.report.Reporter;
import com.chutneytesting.task.spi.FinallyAction;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

public class DefaultExecutionEngineTest {

    private final StepDataEvaluator dataEvaluator = mock(StepDataEvaluator.class, Answers.RETURNS_DEEP_STUBS);
    private final StepExecutionStrategies stepExecutionStrategies = mock(StepExecutionStrategies.class, Answers.RETURNS_DEEP_STUBS);
    private final DelegationService delegationService = mock(DelegationService.class, Answers.RETURNS_DEEP_STUBS);
    private final String fakeEnvironment = "";

    @Test
    public void runtime_exception_should_be_catch_by_fault_barrier() {
        //Given
        StepExecutionStrategy strategy = mock(StepExecutionStrategy.class);
        when(stepExecutionStrategies.buildStrategyFrom(any())).thenReturn(strategy);
        when(strategy.execute(any(), any(), any(), any())).thenThrow(new RuntimeException("Should be catch by fault barrier"));

        Reporter reporter = new Reporter();
        DefaultExecutionEngine engine = new DefaultExecutionEngine(dataEvaluator, stepExecutionStrategies, delegationService, reporter);
        StrategyProperties strategyProperties = new StrategyProperties();
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("retry", strategyProperties);
        StepDefinition stepDefinition = new StepDefinition("name", null, "type", strategyDefinition, null, null, null, null, fakeEnvironment);

        //When
        Long executionId = engine.execute(stepDefinition, ScenarioExecution.createScenarioExecution());
        StepExecutionReport report = reporter.subscribeOnExecution(executionId).blockingLast();

        Assertions.assertThat(executionId).isNotNull();
        Assertions.assertThat(report).isNotNull();
        Assertions.assertThat(report.errors).hasSize(1);
        Assertions.assertThat(report.errors.get(0)).isEqualTo("Should be catch by fault barrier");
    }

    @Test
    public void should_execute_finally_actions_on_execution_end() {
        //Given
        Reporter reporter = new Reporter();
        DefaultExecutionEngine engineUnderTest = new DefaultExecutionEngine(dataEvaluator, stepExecutionStrategies, delegationService, reporter);

        StepExecutionStrategy strategy = mock(StepExecutionStrategy.class);
        when(stepExecutionStrategies.buildStrategyFrom(any())).thenReturn(strategy);
        when(strategy.execute(any(), any(), any(), any())).thenReturn(Status.STOPPED);

        StrategyProperties strategyProperties = new StrategyProperties();
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("retry", strategyProperties);
        StepDefinition stepDefinition = new StepDefinition("name", null, "type", strategyDefinition, null, null, null, null, fakeEnvironment);

        ScenarioExecution mockScenarioExecution = mock(ScenarioExecution.class);

        // When
        engineUnderTest.execute(stepDefinition, mockScenarioExecution);
        reporter.subscribeOnExecution(mockScenarioExecution.executionId).blockingLast();

        // Then
        verify(mockScenarioExecution, times(1)).executeFinallyActions(any(), any());
    }

    @Test
    public void should_execute_finally_actions_on_scenario_stop() {
        //Given
        Reporter reporter = new Reporter();
        DefaultExecutionEngine engineUnderTest = new DefaultExecutionEngine(dataEvaluator, stepExecutionStrategies, delegationService, reporter);

        StepExecutionStrategy strategy = mock(StepExecutionStrategy.class);
        when(stepExecutionStrategies.buildStrategyFrom(any())).thenReturn(strategy);
        when(strategy.execute(any(), any(), any(), any())).thenReturn(Status.STOPPED);

        StrategyProperties strategyProperties = new StrategyProperties();
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("retry", strategyProperties);
        StepDefinition stepDefinition = new StepDefinition("name", null, "type", strategyDefinition, null, null, null, null, fakeEnvironment);

        ScenarioExecution scenarioExecution = ScenarioExecution.createScenarioExecution();
        scenarioExecution.registerFinallyAction(FinallyAction.Builder.forAction("final").build());

        List<BeginStepExecutionEvent> events = new ArrayList<>();
        getInstance().register(BeginStepExecutionEvent.class, events::add);

        // When
        engineUnderTest.execute(stepDefinition, scenarioExecution);
        getInstance().post(new StopExecutionAction(scenarioExecution.executionId));
        reporter.subscribeOnExecution(scenarioExecution.executionId).blockingLast();

        // Then
        Step finalStep = events.get(0).step;
        assertThat(finalStep.type()).isEqualTo("final");
    }

    @Test
    public void should_execute_finally_actions_on_runtime_exception() {
        //Given
        StepExecutionStrategy strategy = mock(StepExecutionStrategy.class);
        when(stepExecutionStrategies.buildStrategyFrom(any())).thenReturn(strategy);
        when(strategy.execute(any(), any(), any(), any())).thenThrow(new RuntimeException("Should be catch by fault barrier"));

        Reporter reporter = new Reporter();
        DefaultExecutionEngine engineUnderTest = new DefaultExecutionEngine(dataEvaluator, stepExecutionStrategies, delegationService, reporter);
        StrategyProperties strategyProperties = new StrategyProperties();
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("retry", strategyProperties);
        StepDefinition stepDefinition = new StepDefinition("name", null, "type", strategyDefinition, null, null, null, null, fakeEnvironment);

        ScenarioExecution mockScenarioExecution = mock(ScenarioExecution.class);

        // When
        Long executionId = engineUnderTest.execute(stepDefinition, mockScenarioExecution);
        reporter.subscribeOnExecution(executionId).blockingLast();

        // Then
        verify(mockScenarioExecution, times(1)).executeFinallyActions(any(), any());
    }
}
