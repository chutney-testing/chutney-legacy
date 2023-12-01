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

package com.chutneytesting.engine.domain.execution.strategies;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.action.domain.ActionTemplateLoaders;
import com.chutneytesting.action.domain.DefaultActionTemplateRegistry;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.TestActionTemplateLoader;
import com.chutneytesting.engine.domain.execution.engine.DefaultStepExecutor;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContextImpl;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.evaluation.SpelFunctions;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class DefaultStepExecutionStrategyTest {

    private DefaultStepExecutionStrategy strategy = DefaultStepExecutionStrategy.instance;
    private StepDataEvaluator dataEvaluator = new StepDataEvaluator(new SpelFunctions());
    private StepExecutor stepExecutor = new DefaultStepExecutor(new DefaultActionTemplateRegistry(new ActionTemplateLoaders(Collections.singletonList(new TestActionTemplateLoader()))));

    @Test
    public void should_execute_the_step() {
        Step step = mock(Step.class);
        when(step.subSteps()).thenReturn(Lists.newArrayList());

        strategy.execute(null, step, null, null);

        verify(step, times(1)).execute(any(), any(), any());
    }

    @Test
    public void when_all_execution_succeed_global_status_is_ok() {
        Step step = buildSample("success", "success");

        Status status = strategy.execute(ScenarioExecution.createScenarioExecution(null), step, new ScenarioContextImpl(), new StepExecutionStrategies(Sets.newHashSet(DefaultStepExecutionStrategy.instance)));
        Assertions.assertThat(status).isEqualTo(Status.SUCCESS);

        Map<String, Status> expectedStatusByStepName = new HashMap<>();
        expectedStatusByStepName.put("sample 1", Status.SUCCESS);
        expectedStatusByStepName.put("step 1", Status.SUCCESS);
        expectedStatusByStepName.put("step 1.1", Status.SUCCESS);
        expectedStatusByStepName.put("step 1.1.1", Status.SUCCESS);
        expectedStatusByStepName.put("step 2", Status.SUCCESS);
        expectedStatusByStepName.put("step 2.1", Status.SUCCESS);

        SoftAssertions softly = new SoftAssertions();
        visit(step, subStep -> softly.assertThat(subStep.status()).isEqualTo(expectedStatusByStepName.get(getStepName(subStep))));
        softly.assertAll();
    }

    @Test
    public void when_early_step_execution_fails_global_status_is_ko_and_next_steps_are_not_runned() {
        Step step = buildSample("fail", "fail");

        Status status = strategy.execute(ScenarioExecution.createScenarioExecution(null), step, new ScenarioContextImpl(), new StepExecutionStrategies(Sets.newHashSet(DefaultStepExecutionStrategy.instance)));
        Assertions.assertThat(status).isEqualTo(Status.FAILURE);

        Map<String, Status> expectedStatusByStepName = new HashMap<>();
        expectedStatusByStepName.put("sample 1", Status.FAILURE);
        expectedStatusByStepName.put("step 1", Status.FAILURE);
        expectedStatusByStepName.put("step 1.1", Status.FAILURE);
        expectedStatusByStepName.put("step 1.1.1", Status.FAILURE);
        expectedStatusByStepName.put("step 2", Status.NOT_EXECUTED);
        expectedStatusByStepName.put("step 2.1", Status.NOT_EXECUTED);

        SoftAssertions softly = new SoftAssertions();
        visit(step, subStep -> softly.assertThat(subStep.status()).isEqualTo(expectedStatusByStepName.get(getStepName(subStep))));
        softly.assertAll();
    }

    @Test
    public void when_later_step_execution_fails_global_status_is_ko_meanwhile_first_steps_are_ok() {
        Step step = buildSample("success", "fail");

        Status status = strategy.execute(ScenarioExecution.createScenarioExecution(null), step, new ScenarioContextImpl(), new StepExecutionStrategies(Sets.newHashSet(DefaultStepExecutionStrategy.instance)));
        Assertions.assertThat(status).isEqualTo(Status.FAILURE);

        Map<String, Status> expectedStatusByStepName = new HashMap<>();
        expectedStatusByStepName.put("sample 1", Status.FAILURE);
        expectedStatusByStepName.put("step 1", Status.SUCCESS);
        expectedStatusByStepName.put("step 1.1", Status.SUCCESS);
        expectedStatusByStepName.put("step 1.1.1", Status.SUCCESS);
        expectedStatusByStepName.put("step 2", Status.FAILURE);
        expectedStatusByStepName.put("step 2.1", Status.FAILURE);

        SoftAssertions softly = new SoftAssertions();
        visit(step, subStep -> softly.assertThat(subStep.status()).isEqualTo(expectedStatusByStepName.get(getStepName(subStep))));
        softly.assertAll();
    }

    private static String getStepName(Step step) {
        StepDefinition stepDefinition = (StepDefinition) ReflectionTestUtils.getField(step, "definition");
        return (String) ReflectionTestUtils.getField(stepDefinition, "name");
    }

    private Step buildSample(String type_111, String type_21) {
        return buildStep("sample 1", "fake-type",
            buildStep("step 1", "fake-type", buildStep("step 1.1", "fake-type", buildStep("step 1.1.1", type_111))),
            buildStep("step 2", "fake-type", buildStep("step 2.1", type_21))
        );
    }

    private Step buildStep(String name, String type, Step... subSteps) {
        return new Step(dataEvaluator, buildStepDef(name, type), stepExecutor, Arrays.asList(subSteps));
    }

    private StepDefinition buildStepDef(String name, String type) {
        return new StepDefinition(name, null, type, null, null, null, null, null, "");
    }

    private static void visit(Step step, Consumer<Step> action) {
        step.subSteps().forEach(subStep -> visit(subStep, action));
        action.accept(step);
    }
}
