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

package com.chutneytesting.engine.domain.execution.engine;

import static com.chutneytesting.engine.domain.execution.ScenarioExecution.createScenarioExecution;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.action.domain.ActionTemplateRegistry;
import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.engine.domain.delegation.DelegationService;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.evaluation.SpelFunctionCallback;
import com.chutneytesting.engine.domain.execution.evaluation.SpelFunctions;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.chutneytesting.engine.domain.execution.strategies.DefaultStepExecutionStrategy;
import com.chutneytesting.engine.domain.execution.strategies.SoftAssertStrategy;
import com.chutneytesting.engine.domain.execution.strategies.StepExecutionStrategies;
import com.chutneytesting.engine.domain.execution.strategies.StepExecutionStrategy;
import com.chutneytesting.engine.domain.report.Reporter;
import com.chutneytesting.tools.loader.ExtensionLoaders;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.util.ReflectionUtils;

class DefaultExecutionEngineTest {

    private final StepDataEvaluator dataEvaluator = mock(StepDataEvaluator.class);
    private final StepExecutionStrategies stepExecutionStrategies = mock(StepExecutionStrategies.class);
    private final DelegationService delegationService = mock(DelegationService.class);
    private final String fakeEnvironment = "env";
    private final ExecutorService actionExecutor = Executors.newSingleThreadExecutor();
    public static final String tearDownRootNodeName = "TearDown";
    private static final String throwableToCatchMessage = "Should be caught by fault barrier";
    private final Dataset emptyDataset = new Dataset(emptyMap(), emptyList());

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("execution_throwable")
    void protect_step_definition_execution_by_fault_barrier_unless_vm_error(Supplier<Throwable> throwable) {
        // Given
        StepExecutionStrategy strategy = mock(StepExecutionStrategy.class);
        when(strategy.execute(any(), any(), any(), any())).thenThrow(throwable.get());
        when(stepExecutionStrategies.buildStrategyFrom(any())).thenReturn(strategy);

        Reporter reporter = new Reporter();
        DefaultExecutionEngine sut = new DefaultExecutionEngine(dataEvaluator, stepExecutionStrategies, delegationService, reporter, actionExecutor);
        StepDefinition stepDefinition = new StepDefinition("name", null, "type", null, null, null, null, null, fakeEnvironment);

        // When
        Long executionId = sut.execute(stepDefinition, emptyDataset, createScenarioExecution(null));
        assertThat(executionId).isNotNull();
        StepExecutionReport report = reporter.subscribeOnExecution(executionId).blockingLast();
        assertThat(report).isNotNull();

        // Then
        if (throwable.get() instanceof VirtualMachineError) {
            assertThat(report.status).isEqualTo(Status.NOT_EXECUTED);
            assertThat(report.errors).hasSize(0);
        } else {
            assertThat(report.status).isEqualTo(Status.FAILURE);
            assertThat(report.errors).hasSize(1);
            assertThat(report.errors.get(0)).isEqualTo(throwableToCatchMessage);
        }
    }

    @Test
    void execute_tear_down_after_step_definition_attaching_it_to_root_step() {
        // Given
        StepExecutionStrategy strategy = mock(StepExecutionStrategy.class);
        when(strategy.execute(any(), any(), any(), any()))
            .thenReturn(Status.SUCCESS) // for step definition
            .thenReturn(Status.SUCCESS); // for tear down step
        when(stepExecutionStrategies.buildStrategyFrom(any()))
            .thenReturn(strategy) // for step definition
            .thenReturn(strategy); // for tear down step

        ScenarioExecution scenarioExecution = createScenarioExecution(null);
        FinallyAction finallyAction = FinallyAction.Builder.forAction("final", "action name").build();
        scenarioExecution.registerFinallyAction(finallyAction);

        Reporter reporter = new Reporter();
        DefaultExecutionEngine sut = new DefaultExecutionEngine(dataEvaluator, stepExecutionStrategies, delegationService, reporter, actionExecutor);
        StepDefinition stepDefinition = new StepDefinition("name", null, "type", null, null, null, null, null, fakeEnvironment);

        // When
        Long executionId = sut.execute(stepDefinition, emptyDataset, scenarioExecution);
        assertThat(executionId).isNotNull();
        StepExecutionReport report = reporter.subscribeOnExecution(executionId).blockingLast();
        assertThat(report).isNotNull();

        // Then
        assertThat(scenarioExecution.hasToStop()).isFalse();

        assertThat(report.steps).hasSize(1);
        assertThat(report.environment).isEqualTo(fakeEnvironment);
        StepExecutionReport tearDownRootNode = report.steps.get(0);
        assertThat(tearDownRootNode.name).isEqualTo(tearDownRootNodeName);
        assertThat(tearDownRootNode.strategy).isEqualTo(new SoftAssertStrategy().getType());
        assertThat(tearDownRootNode.environment).isEqualTo(fakeEnvironment);
        assertThat(tearDownRootNode.steps).hasSize(1);
        assertThat(tearDownRootNode.steps.get(0).name).isEqualTo(finallyAction.name());
        assertThat(tearDownRootNode.steps.get(0).type).isEqualTo(finallyAction.type());
        assertThat(tearDownRootNode.steps.get(0).environment).isEqualTo(fakeEnvironment);
    }

    @Test
    void should_set_environment_in_scenario_context() {
        // Given
        StepExecutionStrategy strategy = DefaultStepExecutionStrategy.instance;
        when(stepExecutionStrategies.buildStrategyFrom(any()))
            .thenReturn(strategy);
        ActionTemplateRegistry actionTemplateRegistry = mock(ActionTemplateRegistry.class);
        when(delegationService.findExecutor(any()))
            .thenReturn(new DefaultStepExecutor(actionTemplateRegistry));
        when(actionTemplateRegistry.getByIdentifier(any()))
            .thenReturn(Optional.empty());

        ScenarioExecution scenarioExecution = createScenarioExecution(null);
        Reporter reporter = new Reporter();
        DefaultExecutionEngine sut = new DefaultExecutionEngine(new StepDataEvaluator(null), stepExecutionStrategies, delegationService, reporter, actionExecutor);

        String environment = "FakeTestEnvironment";
        Map<String, Object> inputs = singletonMap("currentEnvironment", "${#environment}");
        StepDefinition stepDefinition = new StepDefinition("fakeScenario", null, "", null, inputs, null, null, null, environment);

        // When
        Long executionId = sut.execute(stepDefinition, emptyDataset, scenarioExecution);
        assertThat(executionId).isNotNull();
        StepExecutionReport report = reporter.subscribeOnExecution(executionId).blockingLast();

        // Then
        assertThat(report).isNotNull();
        assertThat(report.scenarioContext).contains(entry("environment", environment));
    }

    @Test
    void should_set_dataset_and_constants_in_scenario_context() {
        // Given
        when(stepExecutionStrategies.buildStrategyFrom(any())).thenReturn(DefaultStepExecutionStrategy.instance);

        ActionTemplateRegistry actionTemplateRegistry = mock(ActionTemplateRegistry.class);
        when(actionTemplateRegistry.getByIdentifier(any())).thenReturn(Optional.empty());
        when(delegationService.findExecutor(any())).thenReturn(new DefaultStepExecutor(actionTemplateRegistry));

        StepDefinition stepDefinition = new StepDefinition("fakeScenario", null, "", null, emptyMap(), emptyList(), emptyMap(), emptyMap(), "env");

        Map<String, String> constants = Map.of("keyA", "A", "keyB", "B");
        List<Map<String, String>> datatable = List.of(singletonMap("key", "X"), singletonMap("key", "Y"));
        Dataset dataset = new Dataset(constants, datatable);

        Reporter reporter = new Reporter();

        DefaultExecutionEngine sut = new DefaultExecutionEngine(new StepDataEvaluator(null), stepExecutionStrategies, delegationService, reporter, actionExecutor);

        // When
        Long executionId = sut.execute(stepDefinition, dataset, createScenarioExecution(null));
        StepExecutionReport report = reporter.subscribeOnExecution(executionId).blockingLast();

        // Then
        assertThat(report.scenarioContext).contains(entry("keyA", "A"));
        assertThat(report.scenarioContext).contains(entry("keyB", "B"));
        assertThat(report.scenarioContext).contains(entry("dataset", datatable));
    }

    @Test
    public void should_evaluate_dataset_constants() {
        when(stepExecutionStrategies.buildStrategyFrom(any())).thenReturn(DefaultStepExecutionStrategy.instance);

        ActionTemplateRegistry actionTemplateRegistry = mock(ActionTemplateRegistry.class);
        when(actionTemplateRegistry.getByIdentifier(any())).thenReturn(Optional.empty());
        when(delegationService.findExecutor(any())).thenReturn(new DefaultStepExecutor(actionTemplateRegistry));

        StepDefinition stepDefinition = new StepDefinition("fakeScenario", null, "", null, emptyMap(), emptyList(), emptyMap(), emptyMap(), "env");

        Map<String, String> constants = Map.of("myID", "${#randomID()}");
        Dataset dataset = new Dataset(constants, emptyList());

        Reporter reporter = new Reporter();

        SpelFunctionCallback spelFunctionCallback = new SpelFunctionCallback();
        ExtensionLoaders
            .classpathToClass("META-INF/extension/chutney.functions")
            .load().forEach(c -> ReflectionUtils.doWithMethods(c, spelFunctionCallback));
        SpelFunctions functions = spelFunctionCallback.getSpelFunctions();
        DefaultExecutionEngine sut = new DefaultExecutionEngine(new StepDataEvaluator(functions), stepExecutionStrategies, delegationService, reporter, actionExecutor);

        // When
        Long executionId = sut.execute(stepDefinition, dataset, createScenarioExecution(null));
        StepExecutionReport report = reporter.subscribeOnExecution(executionId).blockingLast();

        // Then
        assertDoesNotThrow(() -> UUID.fromString((String) report.scenarioContext.get("myID")));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("execution_throwable")
    void protect_tear_down_execution_by_fault_barrier_unless_vm_error(Supplier<Throwable> throwable) {
        // Given
        StepExecutionStrategy strategy = mock(StepExecutionStrategy.class);
        when(strategy.execute(any(), any(), any(), any()))
            .thenReturn(Status.SUCCESS) // for step definition
            .thenThrow(throwable.get()); // for tear down step
        when(stepExecutionStrategies.buildStrategyFrom(any()))
            .thenReturn(strategy) // for step definition
            .thenReturn(strategy); // for tear down step

        Reporter reporter = new Reporter();
        DefaultExecutionEngine sut = new DefaultExecutionEngine(dataEvaluator, stepExecutionStrategies, delegationService, reporter, actionExecutor);
        StepDefinition stepDefinition = new StepDefinition("name", null, "type", null, null, null, null, null, fakeEnvironment);

        ScenarioExecution scenarioExecution = createScenarioExecution(null);
        FinallyAction finallyAction = FinallyAction.Builder.forAction("final", "action name").build();
        scenarioExecution.registerFinallyAction(finallyAction);

        // When
        Long executionId = sut.execute(stepDefinition, emptyDataset, scenarioExecution);
        StepExecutionReport report = reporter.subscribeOnExecution(executionId).blockingLast();

        // Then
        assertThat(scenarioExecution.hasToStop()).isFalse();

        if (throwable.get() instanceof VirtualMachineError) {
            assertThat(report.status).isEqualTo(Status.NOT_EXECUTED);
            assertThat(report.errors).hasSize(0);
        } else {
            assertThat(report.status).isEqualTo(Status.FAILURE);
            assertThat(report.errors).hasSize(0);
            assertThat(report.steps).hasSize(1);
            StepExecutionReport tearDownRootNode = report.steps.get(0);
            assertThat(tearDownRootNode.name).isEqualTo(tearDownRootNodeName);
            assertThat(tearDownRootNode.status).isEqualTo(Status.FAILURE);
            assertThat(tearDownRootNode.errors).hasSize(1);
            assertThat(tearDownRootNode.errors.get(0)).isEqualTo(throwableToCatchMessage);
        }
    }

    @Test
    public void execute_tear_down_steps_in_declaration_order() {
        // Given
        Reporter reporter = new Reporter();
        DefaultExecutionEngine sut = new DefaultExecutionEngine(dataEvaluator, stepExecutionStrategies, delegationService, reporter, actionExecutor);

        StepExecutionStrategy strategy = mock(StepExecutionStrategy.class);
        when(strategy.execute(any(), any(), any(), any()))
            .thenReturn(Status.SUCCESS) // for step definition
            .thenReturn(Status.SUCCESS); // for tear down step
        when(stepExecutionStrategies.buildStrategyFrom(any()))
            .thenReturn(strategy) // for step definition
            .thenReturn(strategy); // for tear down step

        StepDefinition stepDefinition = new StepDefinition("name", null, "type", null, null, null, null, null, fakeEnvironment);

        ScenarioExecution scenarioExecution = createScenarioExecution(null);

        Map<String, Object> inputs1 = Map.of("key1", "value1");
        Map<String, Object> inputs2 = Map.of("key2", "value2");
        FinallyAction tearDownFirstAction = FinallyAction.Builder.forAction("action-type-1", "teardown first action").withInput("entries", inputs1).build();
        FinallyAction tearDownSecondAction = FinallyAction.Builder.forAction("action-type-2", "teardown second action").build();
        FinallyAction tearDownThirdAction = FinallyAction.Builder.forAction("action-type-3", "teardown third action").withInput("entries", inputs2).build();
        scenarioExecution.registerFinallyAction(tearDownFirstAction);
        scenarioExecution.registerFinallyAction(tearDownSecondAction);
        scenarioExecution.registerFinallyAction(tearDownThirdAction);

        // When
        Long executionId = sut.execute(stepDefinition, emptyDataset, scenarioExecution);
        StepExecutionReport report = reporter.subscribeOnExecution(executionId).blockingLast();

        // Then
        //Test order is reversed
        assertThat(report.status).isEqualTo(Status.NOT_EXECUTED);
        assertThat(report.steps).hasSize(1);
        StepExecutionReport tearDownRootNode = report.steps.get(0);
        assertThat(tearDownRootNode.name).isEqualTo(tearDownRootNodeName);

        assertThat(tearDownRootNode.steps).hasSize(3);
        StepExecutionReport tearDownFirstActionReport = tearDownRootNode.steps.get(0);
        StepExecutionReport tearDownSecondActionReport = tearDownRootNode.steps.get(1);
        StepExecutionReport tearDownThirdActionReport = tearDownRootNode.steps.get(2);

        assertThat(tearDownFirstActionReport.name).isEqualTo(tearDownFirstAction.name());
        assertThat(tearDownFirstActionReport.type).isEqualTo(tearDownFirstAction.type());
        assertThat(tearDownSecondActionReport.name).isEqualTo(tearDownSecondAction.name());
        assertThat(tearDownSecondActionReport.type).isEqualTo(tearDownSecondAction.type());
        assertThat(tearDownThirdActionReport.name).isEqualTo(tearDownThirdAction.name());
        assertThat(tearDownThirdActionReport.type).isEqualTo(tearDownThirdAction.type());
    }

    private static Stream<Arguments> execution_throwable() {
        return Stream.of(
            Arguments.of(Named.of("RuntimeException", (Supplier<Throwable>) () -> new RuntimeException(throwableToCatchMessage))),
            Arguments.of(Named.of("NoClassDefFoundError", (Supplier<Throwable>) () -> new NoClassDefFoundError(throwableToCatchMessage))),
            Arguments.of(Named.of("NoSuchMethodError", (Supplier<Throwable>) () -> new NoSuchMethodError(throwableToCatchMessage))),
            Arguments.of(Named.of("Error", (Supplier<Throwable>) () -> new Error(throwableToCatchMessage)))
            // Produce flaky tests with parallel run. So we test only what we catch. Not gotta test 'em all!
            /*Arguments.of(Named.of("OutOfMemoryError", (Supplier<Throwable>) () -> new OutOfMemoryError(throwableToNotCatchMessage))),
            Arguments.of(Named.of("StackOverflowError", (Supplier<Throwable>) () -> new StackOverflowError(throwableToNotCatchMessage)))*/
        );
    }
}
