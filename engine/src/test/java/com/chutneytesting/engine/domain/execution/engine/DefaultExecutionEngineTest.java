package com.chutneytesting.engine.domain.execution.engine;

import static com.chutneytesting.engine.domain.execution.RxBus.getInstance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.domain.delegation.DelegationService;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.event.BeginStepExecutionEvent;
import com.chutneytesting.engine.domain.execution.event.EndScenarioExecutionEvent;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.chutneytesting.engine.domain.execution.strategies.DefaultStepExecutionStrategy;
import com.chutneytesting.engine.domain.execution.strategies.SoftAssertStrategy;
import com.chutneytesting.engine.domain.execution.strategies.StepExecutionStrategies;
import com.chutneytesting.engine.domain.execution.strategies.StepExecutionStrategy;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StrategyProperties;
import com.chutneytesting.engine.domain.report.Reporter;
import com.chutneytesting.task.spi.FinallyAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

public class DefaultExecutionEngineTest {

    private final StepDataEvaluator dataEvaluator = mock(StepDataEvaluator.class);
    private final StepExecutionStrategies stepExecutionStrategies = mock(StepExecutionStrategies.class);
    private final DelegationService delegationService = mock(DelegationService.class);
    private final String fakeEnvironment = "env";
    private final Executor taskExecutor = Executors.newFixedThreadPool(1);
    private static final String throwableMessage = "Should be catch by fault barrier";

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("throwable_caught_by_fault_barrier")
    public void linkage_error_and_runtime_exception_should_be_catch_by_fault_barrier(Supplier<Throwable> throwable) {
        //Given
        StepExecutionStrategy strategy = mock(StepExecutionStrategy.class);
        when(stepExecutionStrategies.buildStrategyFrom(any())).thenReturn(strategy);
        when(strategy.execute(any(), any(), any(), any())).thenThrow(throwable.get());

        Reporter reporter = new Reporter();
        DefaultExecutionEngine engine = new DefaultExecutionEngine(dataEvaluator, stepExecutionStrategies, delegationService, reporter, taskExecutor);
        StrategyProperties strategyProperties = new StrategyProperties();
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("retry", strategyProperties);
        StepDefinition stepDefinition = new StepDefinition("name", null, "type", strategyDefinition, null, null, null, null, fakeEnvironment);

        //When
        Long executionId = engine.execute(stepDefinition, ScenarioExecution.createScenarioExecution(null));
        StepExecutionReport report = reporter.subscribeOnExecution(executionId).blockingLast();

        assertThat(executionId).isNotNull();
        assertThat(report).isNotNull();
        assertThat(report.errors).hasSize(1);
        assertThat(report.errors.get(0)).isEqualTo(throwableMessage);
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = {"SUCCESS", "FAILURE", "STOPPED"})
    public void should_execute_finally_actions_on_scenario_end(Status endStatus) {
        //Given
        Reporter reporter = new Reporter();
        DefaultExecutionEngine engineUnderTest = new DefaultExecutionEngine(dataEvaluator, stepExecutionStrategies, delegationService, reporter, taskExecutor);

        StepExecutionStrategy strategy = mock(StepExecutionStrategy.class);
        when(stepExecutionStrategies.buildStrategyFrom(any()))
            .thenReturn(strategy) // for step definition
            .thenReturn(new SoftAssertStrategy()); // for final action step
        when(strategy.execute(any(), any(), any(), any())).thenReturn(endStatus);

        StepDefinition stepDefinition = new StepDefinition("name", null, "type", null, null, null, null, null, fakeEnvironment);

        ScenarioExecution scenarioExecution = ScenarioExecution.createScenarioExecution(null);
        FinallyAction finallyAction = FinallyAction.Builder.forAction("final", "task name").build();
        scenarioExecution.registerFinallyAction(finallyAction);

        List<BeginStepExecutionEvent> events = new ArrayList<>();
        getInstance().registerOnExecutionId(BeginStepExecutionEvent.class, scenarioExecution.executionId, e -> events.add((BeginStepExecutionEvent) e));

        // When
        engineUnderTest.execute(stepDefinition, scenarioExecution);
        reporter.subscribeOnExecution(scenarioExecution.executionId).blockingLast();
        await().until(() -> events.size() == 2);

        // Then
        assertThat(scenarioExecution.hasToStop()).isFalse();

        Step finalStep = events.get(0).step;
        assertThat(finalStep.type()).isEqualTo("");
        assertThat(finalStep.definition().name).isEqualTo("TearDown");
        assertThat(finalStep.subSteps().get(0).definition().type).isEqualTo(finallyAction.type());
        assertThat(finalStep.subSteps().get(0).definition().name).isEqualTo(finallyAction.name());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("throwable_caught_by_fault_barrier")
    public void should_execute_finally_actions_on_runtime_exception_or_linkage_error(Supplier<Throwable> throwable) {
        //Given
        StepExecutionStrategy strategy = mock(StepExecutionStrategy.class);
        when(stepExecutionStrategies.buildStrategyFrom(any())).thenReturn(strategy).thenReturn(DefaultStepExecutionStrategy.instance);
        when(strategy.execute(any(), any(), any(), any())).thenThrow(throwable.get());

        Reporter reporter = new Reporter();
        DefaultExecutionEngine engineUnderTest = new DefaultExecutionEngine(dataEvaluator, stepExecutionStrategies, delegationService, reporter, taskExecutor);
        StrategyProperties strategyProperties = new StrategyProperties();
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("retry", strategyProperties);
        StepDefinition stepDefinition = new StepDefinition("name", null, "type", strategyDefinition, null, null, null, null, fakeEnvironment);

        ScenarioExecution scenarioExecution = ScenarioExecution.createScenarioExecution(null);
        FinallyAction finallyAction = FinallyAction.Builder.forAction("final", "task name").build();
        scenarioExecution.registerFinallyAction(finallyAction);

        List<BeginStepExecutionEvent> events = new ArrayList<>();
        getInstance().registerOnExecutionId(BeginStepExecutionEvent.class, scenarioExecution.executionId, e -> events.add((BeginStepExecutionEvent) e));

        // When
        Long executionId = engineUnderTest.execute(stepDefinition, scenarioExecution);
        reporter.subscribeOnExecution(executionId).blockingLast();

        // Then
        assertThat(scenarioExecution.hasToStop()).isFalse();

        Step finalRootStep = events.get(0).step;
        assertThat(finalRootStep.type()).isEqualTo("");
        assertThat(finalRootStep.definition().name).isEqualTo("TearDown");
        assertThat(finalRootStep.definition().steps.get(0).type).isEqualTo(finallyAction.type());
        assertThat(finalRootStep.definition().steps.get(0).name).isEqualTo(finallyAction.name());
    }

    @Test
    public void finally_actions_are_executed_in_declaration_order() {
        // Given
        Reporter reporter = new Reporter();
        DefaultExecutionEngine engineUnderTest = new DefaultExecutionEngine(dataEvaluator, stepExecutionStrategies, delegationService, reporter, taskExecutor);

        StepExecutionStrategy strategy = mock(StepExecutionStrategy.class);
        when(stepExecutionStrategies.buildStrategyFrom(any()))
            .thenReturn(strategy) // for step definition
            .thenReturn(new SoftAssertStrategy()); // for final action step
        when(strategy.execute(any(), any(), any(), any())).thenReturn(Status.SUCCESS);

        StepDefinition stepDefinition = new StepDefinition("name", null, "type", null, null, null, null, null, fakeEnvironment);

        ScenarioExecution scenarioExecution = ScenarioExecution.createScenarioExecution(null);

        Map<String, Object> entries1 = Map.of("key1", "value1");
        Map<String, Object> entries2 = Map.of("key2", "value2");
        FinallyAction first = FinallyAction.Builder.forAction("context-put", "first").withInput("entries", entries1).build();
        FinallyAction second = FinallyAction.Builder.forAction("failure", "second").build();
        FinallyAction third = FinallyAction.Builder.forAction("context-put", "third").withInput("entries", entries2).build();
        scenarioExecution.registerFinallyAction(first);
        scenarioExecution.registerFinallyAction(second);
        scenarioExecution.registerFinallyAction(third);

        List<BeginStepExecutionEvent> events = new ArrayList<>();
        getInstance().registerOnExecutionId(BeginStepExecutionEvent.class, scenarioExecution.executionId, e -> events.add((BeginStepExecutionEvent) e));

        // When
        engineUnderTest.execute(stepDefinition, scenarioExecution);
        reporter.subscribeOnExecution(scenarioExecution.executionId).blockingLast();
        await().until(() -> events.size() == 4);

        // Then
        //Test order is reversed
        Step finalStep = events.get(0).step;
        Step firstStep = events.get(1).step;
        Step secondStep = events.get(2).step;
        Step thirdStep = events.get(3).step;

        assertThat(finalStep.isParentStep()).isTrue();

        assertThat(thirdStep.type()).isEqualTo("context-put");
        Map<String, Object> e1 = (Map<String, Object>) thirdStep.definition().inputs.get("entries");
        assertThat(e1).containsAllEntriesOf(entries2);

        assertThat(secondStep.type()).isEqualTo("failure");

        assertThat(firstStep.type()).isEqualTo("context-put");
        Map<String, Object> e2 = (Map<String, Object>) firstStep.definition().inputs.get("entries");
        assertThat(e2).containsAllEntriesOf(entries1);
    }

    @Test
    public void should_add_finally_actions_to_root_step() {
        Reporter reporter = new Reporter();
        DefaultExecutionEngine engineUnderTest = new DefaultExecutionEngine(dataEvaluator, stepExecutionStrategies, delegationService, reporter, taskExecutor);

        StepExecutionStrategy strategy = mock(StepExecutionStrategy.class);
        when(stepExecutionStrategies.buildStrategyFrom(any())).thenReturn(strategy);
        when(strategy.execute(any(), any(), any(), any())).thenReturn(Status.SUCCESS);

        StepDefinition stepDefinition = new StepDefinition("name", null, "type", null, null, null, null, null, fakeEnvironment);

        ScenarioExecution scenarioExecution = ScenarioExecution.createScenarioExecution(null);

        FinallyAction finallyAction = FinallyAction.Builder.forAction("final", "finalActionName").build();
        scenarioExecution.registerFinallyAction(finallyAction);

        AtomicReference<EndScenarioExecutionEvent> endEvent = new AtomicReference<>();
        getInstance().registerOnExecutionId(EndScenarioExecutionEvent.class, scenarioExecution.executionId, e -> endEvent.set((EndScenarioExecutionEvent) e));

        // When
        engineUnderTest.execute(stepDefinition, scenarioExecution);
        reporter.subscribeOnExecution(scenarioExecution.executionId).blockingLast();
        await().untilAtomic(endEvent, notNullValue());

        // Then
        Step rootStep = endEvent.get().step;
        assertThat(rootStep.subSteps().size()).isEqualTo(1);
        assertThat(rootStep.subSteps().get(0).definition().name).isEqualTo("TearDown");
        assertThat(rootStep.subSteps().get(0).definition().environment).isEqualTo(fakeEnvironment);
        assertThat(rootStep.subSteps().get(0).definition().steps.size()).isEqualTo(1);
        assertThat(rootStep.subSteps().get(0).definition().steps.get(0).name).isEqualTo(finallyAction.name());
        assertThat(rootStep.subSteps().get(0).definition().steps.get(0).environment).isEqualTo(fakeEnvironment);
    }

    private static Stream<Arguments> throwable_caught_by_fault_barrier() {
        return Stream.of(
            Arguments.of(Named.of("RuntimeException", (Supplier<Throwable>) () -> new RuntimeException(throwableMessage))),
            Arguments.of(Named.of("NoClassDefFoundError", (Supplier<Throwable>) () -> new NoClassDefFoundError(throwableMessage))),
            Arguments.of(Named.of("NoSuchMethodError", (Supplier<Throwable>) () -> new NoSuchMethodError(throwableMessage)))
        );
    }
}
