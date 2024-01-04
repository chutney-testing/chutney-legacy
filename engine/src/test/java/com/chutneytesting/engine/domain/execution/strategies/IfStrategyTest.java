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

import static com.chutneytesting.engine.domain.execution.ScenarioExecution.createScenarioExecution;
import static com.chutneytesting.engine.domain.execution.report.Status.SUCCESS;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.ExecutionConfiguration;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.StatusDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.engine.api.execution.TestEngine;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContextImpl;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.evaluation.SpelFunctions;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.tools.Jsons;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class IfStrategyTest {

    static Stream<Arguments> datasetForIf_strategy_nominal_cases() {
        return Stream.of(
            of("Should not execute step if condition false", false, 0, SUCCESS, null),
            of("Should execute step if condition true", true, 1, SUCCESS, null, null),
            of("Should not execute step if condition false Spel", "${ (1+1) == 3}", 0, SUCCESS, null),
            of("Should execute step if condition true Spel", "${ (1+1) == 2}", 1, SUCCESS, null)
        );
    }

    @ParameterizedTest(name = "#{index}: {0}")
    @MethodSource("datasetForIf_strategy_nominal_cases")
    void if_strategy_nominal_cases(String testName, Object ifCondition, Integer stepExecutionNumber, Status expectedStatus) {

        //Given
        StrategyProperties strategyProperties = new StrategyProperties();
        strategyProperties.put("condition", ifCondition);
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("if", strategyProperties);

        Step step = mock(Step.class);
        when(step.strategy()).thenReturn(of(strategyDefinition));
        when(step.execute(any(), any(), any())).thenReturn(SUCCESS);
        StepDataEvaluator dataEvaluator = new StepDataEvaluator(new SpelFunctions());
        when(step.dataEvaluator()).thenReturn(dataEvaluator);
        StepExecutionStrategy ifStrategy = new IfStrategy();

        // When
        Status status = ifStrategy.execute(createScenarioExecution(null), step, new ScenarioContextImpl(), null);

        //Then
        verify(step, times(stepExecutionNumber)).execute(any(), any(), any());
        assertThat(status).isEqualTo(expectedStatus);
        verify(step, times(1)).addInformation("Execution condition [" + ifCondition + "] = " + (stepExecutionNumber == 1 ? "step executed" : "step skipped"));
        if (stepExecutionNumber == 0) {
            verify(step).success();
        }
    }

    static Stream<Arguments> datasetForIf_strategy_error_cases() {
        return Stream.of(
            of("Should return error if spel does not return a Boolan", "i am not a boolean", "Error message: class java.lang.String cannot be cast to class java.lang.Boolean (java.lang.String and java.lang.Boolean are in module java.base of loader 'bootstrap')"),
            of("should return error if spel cannot be evaluated", "${'test'.notExistingMethod}", "Error message: Cannot resolve 'test'.notExistingMethod , EL1008E: Property or field 'notExistingMethod' cannot be found on object of type 'java.lang.String' - maybe not public or not valid?")
        );
    }

    @ParameterizedTest(name = "#{index}: {0}")
    @MethodSource("datasetForIf_strategy_error_cases")
    void if_strategy_error_cases(String testName, Object ifCondition, String errorMessage) {

        //Given
        StrategyProperties strategyProperties = new StrategyProperties();
        strategyProperties.put("condition", ifCondition);
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("if", strategyProperties);

        Step step = mock(Step.class);
        when(step.strategy()).thenReturn(of(strategyDefinition));
        StepDataEvaluator dataEvaluator = new StepDataEvaluator(new SpelFunctions());
        when(step.dataEvaluator()).thenReturn(dataEvaluator);
        StepExecutionStrategy ifStrategy = new IfStrategy();

        // When + Then
        assertThatThrownBy(() -> ifStrategy.execute(createScenarioExecution(null), step, new ScenarioContextImpl(), null))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Cannot evaluate execution condition: [" + ifCondition + "]. " + errorMessage);

    }

    @Test
    void should_throw_error_if_no_strategy_definition() {
        Step step = mock(Step.class);
        when(step.strategy()).thenReturn(empty());

        StepExecutionStrategy ifStrategy = new IfStrategy();
        assertThatThrownBy(() -> ifStrategy.execute(createScenarioExecution(null), step, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Strategy definition cannot be empty");
    }

    @Test
    void should_throw_error_if_condition_property() {
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("if", new StrategyProperties());

        Step step = mock(Step.class);
        when(step.strategy()).thenReturn(of(strategyDefinition));

        StepExecutionStrategy ifStrategy = new IfStrategy();
        assertThatThrownBy(() -> ifStrategy.execute(createScenarioExecution(null), step, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Property [condition] mandatory for if strategy");
    }

    static Stream<Arguments> datasetForIf_strategy_on_parent_stepnominal_cases() {
        return Stream.of(
            of("Should not execute step if condition false", false, 0, SUCCESS, null),
            of("Should execute step if condition true", true, 1, SUCCESS, null, null),
            of("Should not execute step if condition false Spel", "${ (1+1) == 3}", 0, SUCCESS, null),
            of("Should execute step if condition true Spel", "${ (1+1) == 2}", 1, SUCCESS, null)
        );
    }

    @ParameterizedTest(name = "#{index}: {0}")
    @MethodSource("datasetForIf_strategy_on_parent_stepnominal_cases")
    void if_strategy_on_parent_step_nominal_cases(String testName, Object ifCondition, Integer stepExecutionNumber, Status expectedStatus) {

        //Given
        StrategyProperties strategyProperties = new StrategyProperties();
        strategyProperties.put("condition", ifCondition);
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("if", strategyProperties);

        // Build parent with two sub step
        Step step = mock(Step.class);
        when(step.strategy()).thenReturn(of(strategyDefinition));
        when(step.isParentStep()).thenReturn(true);

        List<Step> subSteps = new ArrayList<>();
        Step step1 = mock(Step.class);
        when(step1.execute(any(), any(), any())).thenReturn(SUCCESS);
        when(step1.isParentStep()).thenReturn(false);
        subSteps.add(step1);
        Step step2 = mock(Step.class);
        when(step2.execute(any(), any(), any())).thenReturn(SUCCESS);
        when(step2.isParentStep()).thenReturn(false);
        subSteps.add(step2);
        when(step.subSteps()).thenReturn(subSteps);
        StepDataEvaluator dataEvaluator = new StepDataEvaluator(new SpelFunctions());
        when(step.dataEvaluator()).thenReturn(dataEvaluator);
        when(step1.dataEvaluator()).thenReturn(dataEvaluator);
        when(step2.dataEvaluator()).thenReturn(dataEvaluator);
        StepExecutionStrategy ifStrategy = new IfStrategy();

        // When
        Status status = ifStrategy.execute(createScenarioExecution(null), step, new ScenarioContextImpl(), new StepExecutionStrategies(Sets.newHashSet(DefaultStepExecutionStrategy.instance)));

        //Then
        verify(step, times(0)).execute(any(), any(), any());
        verify(step1, times(stepExecutionNumber)).execute(any(), any(), any());
        verify(step2, times(stepExecutionNumber)).execute(any(), any(), any());
        assertThat(status).isEqualTo(expectedStatus);
        verify(step, times(1)).addInformation("Execution condition [" + ifCondition + "] = " + (stepExecutionNumber == 1 ? "step executed" : "step skipped"));
        if (stepExecutionNumber == 0) {
            verify(step).success();
            verify(step1).success();
            verify(step2).success();
            verify(step1).addInformation(eq("Step skipped"));
            verify(step2).addInformation(eq("Step skipped"));
        }
    }

    static Stream<Arguments> if_strategy_with_sub_parent_step_nominal_cases() {
        return Stream.of(
            of("Should not execute step if condition false", false),
            of("Should execute step if condition true", true),
            of("Should not execute step if condition false Spel", "${ (1+1) == 3}"),
            of("Should execute step if condition true Spel", "${ (1+1) == 2}")
        );
    }

    @ParameterizedTest(name = "#{index}: {0}")
    @MethodSource("if_strategy_with_sub_parent_step_nominal_cases")
    void if_strategy_with_sub_parent_step_nominal_cases(String testName, Object ifCondition) {

        // Given
        StepDataEvaluator dataEvaluator = new StepDataEvaluator(new SpelFunctions());
        Boolean ifConditionEvaluation = (Boolean) dataEvaluator.evaluate(ifCondition, emptyMap());

        StrategyProperties strategyProperties = new StrategyProperties();
        strategyProperties.put("condition", ifCondition);
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("if", strategyProperties);

        Step ifStrategyStep = mock(Step.class);
        when(ifStrategyStep.strategy()).thenReturn(of(strategyDefinition));
        when(ifStrategyStep.dataEvaluator()).thenReturn(dataEvaluator);

        // Step with IfStrategy has two substeps,the first of which has two subSteps
        Step parentSubStep = mock(Step.class);
        Step parentSubStep1 = mock(Step.class);
        when(parentSubStep1.isParentStep()).thenReturn(false);
        when(parentSubStep1.execute(any(), any(), any())).thenReturn(SUCCESS);
        when(parentSubStep1.dataEvaluator()).thenReturn(dataEvaluator);
        Step parentSubStep2 = mock(Step.class);
        when(parentSubStep2.isParentStep()).thenReturn(false);
        when(parentSubStep2.execute(any(), any(), any())).thenReturn(SUCCESS);
        when(parentSubStep2.dataEvaluator()).thenReturn(dataEvaluator);

        when(parentSubStep.isParentStep()).thenReturn(true);
        when(parentSubStep.subSteps()).thenReturn(List.of(parentSubStep1, parentSubStep2));
        when(parentSubStep.dataEvaluator()).thenReturn(dataEvaluator);

        Step simpleSubStep = mock(Step.class);
        when(simpleSubStep.isParentStep()).thenReturn(false);
        when(simpleSubStep.execute(any(), any(), any())).thenReturn(SUCCESS);
        when(simpleSubStep.dataEvaluator()).thenReturn(dataEvaluator);

        when(ifStrategyStep.isParentStep()).thenReturn(true);
        when(ifStrategyStep.subSteps()).thenReturn(List.of(parentSubStep, simpleSubStep));
        when(ifStrategyStep.dataEvaluator()).thenReturn(dataEvaluator);

        // When
        Status status = new IfStrategy()
            .execute(createScenarioExecution(null), ifStrategyStep, new ScenarioContextImpl(), new StepExecutionStrategies(Sets.newHashSet(DefaultStepExecutionStrategy.instance)));

        //Then
        assertThat(status).isEqualTo(SUCCESS);
        verify(ifStrategyStep, times(0)).execute(any(), any(), any());
        if (ifConditionEvaluation) {
            verify(ifStrategyStep).addInformation("Execution condition [" + ifCondition + "] = " + "step executed");
            verify(parentSubStep, times(0)).execute(any(), any(), any());
            List.of(parentSubStep1, parentSubStep2, simpleSubStep).forEach(subStep -> {
                verify(subStep).execute(any(), any(), any());
            });
        } else {
            verify(ifStrategyStep).addInformation("Execution condition [" + ifCondition + "] = " + "step skipped");
            verify(ifStrategyStep).success();
            List.of(parentSubStep, parentSubStep1, parentSubStep2, simpleSubStep).forEach(subStep -> {
                verify(subStep, times(0)).execute(any(), any(), any());
                verify(subStep).addInformation("Step skipped");
                verify(subStep).success();
            });
        }
    }

    @Test
    public void should_resolve_name_from_context_with_strategy_if() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/ifStrategy/if_strategy_step_with_name_resolver_from_context_put.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result).hasFieldOrPropertyWithValue("status", StatusDto.SUCCESS);
        assertThat(result.steps).hasSize(2);
        assertThat(result.steps.get(1).name).isEqualTo("Step 2 Parent : value");
    }
}
