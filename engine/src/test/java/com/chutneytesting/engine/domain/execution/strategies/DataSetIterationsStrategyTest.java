package com.chutneytesting.engine.domain.execution.strategies;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.ExecutionConfiguration;
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
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class DataSetIterationsStrategyTest {

    private final ExecutionConfiguration executionConfiguration = new ExecutionConfiguration();
    private final DefaultStepExecutionStrategy defaultStepExecutionStrategy = DefaultStepExecutionStrategy.instance;
    private final StepDataEvaluator dataEvaluator = new StepDataEvaluator(new SpelFunctions());
    private final StepExecutor stepExecutor = new DefaultStepExecutor(new DefaultTaskTemplateRegistry(new TaskTemplateLoaders(Collections.singletonList(new TestTaskTemplateLoader()))));
    private final StepExecutionStrategies strategies = new StepExecutionStrategies(executionConfiguration.stepExecutionStrategies());

    @Test
    public void should_not_run_next_step_after_iteration_fail_within_default_strategy() {

        Step rootStep =
            buildStep("root step", "fake-type", defaultStepExecutionStrategy,
                buildStep("step 1", "fake-type", new DataSetIterationsStrategy(),
                    buildStep("step 1.1", "success", defaultStepExecutionStrategy),
                    buildStep("step 1.2", "fail", defaultStepExecutionStrategy),
                    buildStep("step 1.3", "success", defaultStepExecutionStrategy)
                ),
                buildStep("step 2", "fake-type", defaultStepExecutionStrategy,
                    buildStep("step 2.1", "fail", defaultStepExecutionStrategy)
                )
            );

        Status status = defaultStepExecutionStrategy.execute(ScenarioExecution.createScenarioExecution(), rootStep, new ScenarioContextImpl(), strategies);
        assertThat(status).isEqualTo(Status.FAILURE);

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
    public void should_not_run_next_step_after_iteration_fail_within_retry_strategy() {
        StrategyProperties strategyProperties = properties();

        Step rootStep =
            buildStep("root step", "fake-type", defaultStepExecutionStrategy,
                buildStep("step 1", "fake-type", new DataSetIterationsStrategy(),
                    buildStep("step 1.1", "success", new RetryWithTimeOutStrategy(), strategyProperties),
                    buildStep("step 1.2", "fail", new RetryWithTimeOutStrategy(), strategyProperties),
                    buildStep("step 1.3", "success", new RetryWithTimeOutStrategy(), strategyProperties)),
                buildStep("step 2", "fake-type", defaultStepExecutionStrategy,
                    buildStep("step 2.1", "fail", defaultStepExecutionStrategy)
                )
            );

        Status status = defaultStepExecutionStrategy.execute(ScenarioExecution.createScenarioExecution(), rootStep, new ScenarioContextImpl(), strategies);
        assertThat(status).isEqualTo(Status.FAILURE);

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

    @Test
    public void should_run_next_step_after_iteration_fail_within_soft_assert_strategy() {

        Step rootStep =
            buildStep("root step", "fake-type", defaultStepExecutionStrategy,
                buildStep("step 1", "fake-type", new DataSetIterationsStrategy(),
                    buildStep("step 1.1", "success", new SoftAssertStrategy()),
                    buildStep("step 1.2", "fail", new SoftAssertStrategy()),
                    buildStep("step 1.3", "success", new SoftAssertStrategy())),
                buildStep("step 2", "fake-type", defaultStepExecutionStrategy,
                    buildStep("step 2.1", "fail", defaultStepExecutionStrategy)
                )
            );

        Status status = defaultStepExecutionStrategy.execute(ScenarioExecution.createScenarioExecution(), rootStep, new ScenarioContextImpl(), strategies);
        assertThat(status).isEqualTo(Status.FAILURE);

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

    private static String getStepName(Step step) {
        StepDefinition stepDefinition = (StepDefinition) ReflectionTestUtils.getField(step, "definition");
        return (String) ReflectionTestUtils.getField(stepDefinition, "name");
    }

    private Step buildStep(String name, String type, StepExecutionStrategy strategy, Step... subSteps) {
        return new Step(dataEvaluator, buildStepDef(name, type, strategy, new StrategyProperties()), Optional.empty(), stepExecutor, Arrays.asList(subSteps));
    }

    private Step buildStep(String name, String type, StepExecutionStrategy strategy, StrategyProperties strategyProperties, Step... subSteps) {
        return new Step(dataEvaluator, buildStepDef(name, type, strategy, strategyProperties), Optional.empty(), stepExecutor, Arrays.asList(subSteps));
    }

    private StepDefinition buildStepDef(String name, String type, StepExecutionStrategy strategy, StrategyProperties strategyProperties) {
        return new StepDefinition(name, null, type, new StepStrategyDefinition(strategy.getType(), strategyProperties), null, null, null, "");
    }

    private static void visit(Step step, Consumer<Step> action) {
        step.subSteps().forEach(subStep -> visit(subStep, action));
        action.accept(step);
    }

    private StrategyProperties properties() {
        StrategyProperties strategyProperties = new StrategyProperties();
        strategyProperties.setProperty("timeOut", "50 ms");
        strategyProperties.setProperty("retryDelay", "5 ms");
        return strategyProperties;
    }

}
