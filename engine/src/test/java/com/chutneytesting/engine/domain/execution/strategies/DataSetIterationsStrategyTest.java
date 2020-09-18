package com.chutneytesting.engine.domain.execution.strategies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.TestTaskTemplateLoader;
import com.chutneytesting.engine.domain.execution.engine.DefaultStepExecutor;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContextImpl;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.evaluation.SpelFunctions;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.task.domain.DefaultTaskTemplateRegistry;
import com.chutneytesting.task.domain.TaskTemplateLoaders;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class DataSetIterationsStrategyTest {

    private DataSetIterationsStrategy dataSetIterationsStrategy = new DataSetIterationsStrategy();
    private DefaultStepExecutionStrategy defaultStepExecutionStrategy = DefaultStepExecutionStrategy.instance;
    private SoftAssertStrategy softAssertStrategy = new SoftAssertStrategy();
    private RetryWithTimeOutStrategy retryWithTimeOutStrategy = new RetryWithTimeOutStrategy();
    private StepDataEvaluator dataEvaluator = new StepDataEvaluator(new SpelFunctions());
    private StepExecutor stepExecutor = new DefaultStepExecutor(new DefaultTaskTemplateRegistry(new TaskTemplateLoaders(Collections.singletonList(new TestTaskTemplateLoader()))));

    @Test
    public void should_not_run_next_step_after_iteration_fail_within_default_strategy() {
        Step rootStep =
            buildStep("root step", "fake-type",
                buildStep("step 1", "fake-type",
                    buildStep("step 1.1", "success"),
                    buildStep("step 1.2", "fail"),
                    buildStep("step 1.3", "success")),
                buildStep("step 2", "fake-type", buildStep("step 2.1", "fail"))
            );
        StepExecutionStrategies strategies = mock(StepExecutionStrategies.class);
        when(strategies.buildStrategyFrom(rootStep)).thenReturn(defaultStepExecutionStrategy);
        when(strategies.buildStrategyFrom(rootStep.subSteps().get(0))).thenReturn(dataSetIterationsStrategy);
        when(strategies.buildStrategyFrom(rootStep.subSteps().get(1))).thenReturn(defaultStepExecutionStrategy);
        when(strategies.buildStrategyFrom(rootStep.subSteps().get(0).subSteps().get(0))).thenReturn(defaultStepExecutionStrategy);
        when(strategies.buildStrategyFrom(rootStep.subSteps().get(0).subSteps().get(1))).thenReturn(defaultStepExecutionStrategy);
        when(strategies.buildStrategyFrom(rootStep.subSteps().get(0).subSteps().get(2))).thenReturn(defaultStepExecutionStrategy);


        Status status = defaultStepExecutionStrategy.execute(ScenarioExecution.createScenarioExecution(), rootStep, new ScenarioContextImpl(), strategies);
        Assertions.assertThat(status).isEqualTo(Status.FAILURE);

        Map<String, Status> expectedStatusByStepName = new HashMap<>();
        expectedStatusByStepName.put("root step", Status.FAILURE);
        expectedStatusByStepName.put("step 1", Status.FAILURE);
        expectedStatusByStepName.put("step 1.1", Status.SUCCESS);
        expectedStatusByStepName.put("step 1.2", Status.FAILURE);
        expectedStatusByStepName.put("step 1.3", Status.SUCCESS);
        expectedStatusByStepName.put("step 2", Status.NOT_EXECUTED);
        expectedStatusByStepName.put("step 2.1", Status.NOT_EXECUTED);

        SoftAssertions softly = new SoftAssertions();
        visit(rootStep, subStep -> softly.assertThat(subStep.status()).isEqualTo(expectedStatusByStepName.get(getStepName(subStep))));
        softly.assertAll();
    }

    @Test
    public void should_run_next_step_after_iteration_fail_within_soft_assert_strategy() {
        Step rootStep =
            buildStep("root step", "fake-type",
                buildStep("step 1", "fake-type",
                    buildStep("step 1.1", "success"),
                    buildStep("step 1.2", "fail"),
                    buildStep("step 1.3", "success")),
                buildStep("step 2", "fake-type", buildStep("step 2.1", "fail"))
            );
        StepExecutionStrategies strategies = mock(StepExecutionStrategies.class);
        when(strategies.buildStrategyFrom(rootStep)).thenReturn(defaultStepExecutionStrategy);
        when(strategies.buildStrategyFrom(rootStep.subSteps().get(0))).thenReturn(dataSetIterationsStrategy);
        when(strategies.buildStrategyFrom(rootStep.subSteps().get(1))).thenReturn(defaultStepExecutionStrategy);
        when(strategies.buildStrategyFrom(rootStep.subSteps().get(0).subSteps().get(0))).thenReturn(softAssertStrategy);
        when(strategies.buildStrategyFrom(rootStep.subSteps().get(0).subSteps().get(1))).thenReturn(softAssertStrategy);
        when(strategies.buildStrategyFrom(rootStep.subSteps().get(0).subSteps().get(2))).thenReturn(softAssertStrategy);


        Status status = defaultStepExecutionStrategy.execute(ScenarioExecution.createScenarioExecution(), rootStep, new ScenarioContextImpl(), strategies);
        Assertions.assertThat(status).isEqualTo(Status.FAILURE);

        Map<String, Status> expectedStatusByStepName = new HashMap<>();
        expectedStatusByStepName.put("root step", Status.FAILURE);
        expectedStatusByStepName.put("step 1", Status.FAILURE);
        expectedStatusByStepName.put("step 1.1", Status.SUCCESS);
        expectedStatusByStepName.put("step 1.2", Status.FAILURE);
        expectedStatusByStepName.put("step 1.3", Status.SUCCESS);
        expectedStatusByStepName.put("step 2", Status.FAILURE);
        expectedStatusByStepName.put("step 2.1", Status.FAILURE);

        SoftAssertions softly = new SoftAssertions();
        visit(rootStep, subStep -> softly.assertThat(subStep.status()).isEqualTo(expectedStatusByStepName.get(getStepName(subStep))));
        softly.assertAll();
    }

    @Test
    public void should_not_run_next_step_after_iteration_fail_within_retry_strategy() {
        StrategyProperties strategyProperties = properties("50 ms", "5 ms");
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("", strategyProperties);
        Step rootStep =
            buildStep("root step", "fake-type",
                buildStep("step 1", "fake-type",
                    buildStep("step 1.1", "success", strategyDefinition),
                    buildStep("step 1.2", "fail", strategyDefinition),
                    buildStep("step 1.3", "success", strategyDefinition)),
                buildStep("step 2", "fake-type", buildStep("step 2.1", "fail"))
            );


        StepExecutionStrategies strategies = mock(StepExecutionStrategies.class);
        when(strategies.buildStrategyFrom(rootStep)).thenReturn(defaultStepExecutionStrategy);
        when(strategies.buildStrategyFrom(rootStep.subSteps().get(0))).thenReturn(dataSetIterationsStrategy);
        when(strategies.buildStrategyFrom(rootStep.subSteps().get(1))).thenReturn(defaultStepExecutionStrategy);
        when(strategies.buildStrategyFrom(rootStep.subSteps().get(0).subSteps().get(0))).thenReturn(retryWithTimeOutStrategy);
        when(strategies.buildStrategyFrom(rootStep.subSteps().get(0).subSteps().get(1))).thenReturn(retryWithTimeOutStrategy);
        when(strategies.buildStrategyFrom(rootStep.subSteps().get(0).subSteps().get(2))).thenReturn(retryWithTimeOutStrategy);

        Status status = defaultStepExecutionStrategy.execute(ScenarioExecution.createScenarioExecution(), rootStep, new ScenarioContextImpl(), strategies);
        Assertions.assertThat(status).isEqualTo(Status.FAILURE);

        Map<String, Status> expectedStatusByStepName = new HashMap<>();
        expectedStatusByStepName.put("root step", Status.FAILURE);
        expectedStatusByStepName.put("step 1", Status.FAILURE);
        expectedStatusByStepName.put("step 1.1", Status.SUCCESS);
        expectedStatusByStepName.put("step 1.2", Status.FAILURE);
        expectedStatusByStepName.put("step 1.3", Status.SUCCESS);
        expectedStatusByStepName.put("step 2", Status.NOT_EXECUTED);
        expectedStatusByStepName.put("step 2.1", Status.NOT_EXECUTED);

        SoftAssertions softly = new SoftAssertions();
        visit(rootStep, subStep -> softly.assertThat(subStep.status()).isEqualTo(expectedStatusByStepName.get(getStepName(subStep))));
        softly.assertAll();

        assertThat(rootStep.subSteps().get(0).subSteps().get(0).informations().get(1)).isEqualTo("Try number : 1");
        assertThat(rootStep.subSteps().get(0).subSteps().get(1).informations().get(1)).isGreaterThan("Try number : 1");
        assertThat(rootStep.subSteps().get(0).subSteps().get(2).informations().get(1)).isEqualTo("Try number : 1");

    }


    private static String getStepName(Step step) {
        StepDefinition stepDefinition = (StepDefinition) ReflectionTestUtils.getField(step, "definition");
        return (String) ReflectionTestUtils.getField(stepDefinition, "name");
    }

    private Step buildStep(String name, String type, StepStrategyDefinition strategy, Step... subSteps) {
        return new Step(dataEvaluator, buildStepDef(name, type, strategy), Optional.empty(), stepExecutor, Arrays.asList(subSteps));
    }

    private Step buildStep(String name, String type, Step... subSteps) {
        return new Step(dataEvaluator, buildStepDef(name, type, null), Optional.empty(), stepExecutor, Arrays.asList(subSteps));
    }

    private StepDefinition buildStepDef(String name, String type, StepStrategyDefinition strategy) {
        return new StepDefinition(name, null, type, strategy, null, null, null, "");
    }

    private static void visit(Step step, Consumer<Step> action) {
        step.subSteps().forEach(subStep -> visit(subStep, action));
        action.accept(step);
    }

    private StrategyProperties properties(String timeOut, String retryDelay) {
        StrategyProperties strategyProperties = new StrategyProperties();
        strategyProperties.setProperty("timeOut", timeOut);
        strategyProperties.setProperty("retryDelay", retryDelay);

        return strategyProperties;
    }

}
