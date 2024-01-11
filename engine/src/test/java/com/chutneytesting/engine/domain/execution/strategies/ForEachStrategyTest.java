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

import static com.chutneytesting.engine.api.execution.StatusDto.FAILURE;
import static com.chutneytesting.engine.api.execution.StatusDto.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.chutneytesting.ExecutionConfiguration;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.engine.api.execution.TestEngine;
import com.chutneytesting.tools.Jsons;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ForEachStrategyTest {

    @Test
    public void should_fail_if_no_dataset_with_iteration_strategy() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/forEachStrategy/error_without_dataset_iterations.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result).hasFieldOrPropertyWithValue("status", FAILURE);
    }

    @Test
    public void should_resolve_name_from_context_with_iteration_strategy() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/forEachStrategy/iteration_strategy_step_with_name_resolver_from_context_put.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result).hasFieldOrPropertyWithValue("status", SUCCESS);
        assertThat(result.steps).hasSize(2);
        assertThat(result.steps.get(1).name).isEqualTo("Step 2 Parent : value");
        assertThat(result.steps.get(1).steps).hasSize(2);
        assertThat(result.steps.get(1).steps.get(0).name).isEqualTo("Step 2 Parent : value");
        assertThat(result.steps.get(1).steps.get(1).name).isEqualTo("Step 2 Parent : value");
    }

    @Test
    public void should_resolve_name_with_idx_in_child_from_dataset_of_parent_of_parent() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/forEachStrategy/iteration_strategy_using_idx_in_sub_sub_step.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result).hasFieldOrPropertyWithValue("status", SUCCESS);
        assertThat(result.name).isEqualTo("<i> - parent");
        assertThat(result.steps).hasSize(1);
        assertThat(result.steps.get(0).name).isEqualTo("0 - parent");
        assertThat(result.steps.get(0).steps).hasSize(1);
        assertThat(result.steps.get(0).steps.get(0).name).isEqualTo("0 sub parent");
        assertThat(result.steps.get(0).steps.get(0).steps).hasSize(1);
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).name).isEqualTo("0 sub sub parent");
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).steps).hasSize(1);
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).steps.get(0).name).isEqualTo("0 child");
    }

    @Test
    public void should_resolve_name_in_child_with_value_from_dataset_of_parent_of_parent() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/forEachStrategy/iteration_strategy_using_value_in_sub_sub_sub_step.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result).hasFieldOrPropertyWithValue("status", SUCCESS);
        assertThat(result.name).isEqualTo("${#env} - parent");
        assertThat(result.steps).hasSize(2);
        assertThat(result.steps.get(0).name).isEqualTo("env0 - parent");
        assertThat(result.steps.get(0).steps).hasSize(1);
        assertThat(result.steps.get(0).steps.get(0).name).isEqualTo("env0 sub parent");
        assertThat(result.steps.get(0).steps.get(0).steps).hasSize(1);
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).name).isEqualTo("env0 sub sub parent");
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).steps).hasSize(1);
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).steps.get(0).name).isEqualTo("env0 child");
        assertThat(result.steps.get(1).name).isEqualTo("env1 - parent");
        assertThat(result.steps.get(1).steps).hasSize(1);
        assertThat(result.steps.get(1).steps.get(0).name).isEqualTo("env1 sub parent");
        assertThat(result.steps.get(1).steps.get(0).steps).hasSize(1);
        assertThat(result.steps.get(1).steps.get(0).steps.get(0).name).isEqualTo("env1 sub sub parent");
        assertThat(result.steps.get(1).steps.get(0).steps.get(0).steps).hasSize(1);
        assertThat(result.steps.get(1).steps.get(0).steps.get(0).steps.get(0).name).isEqualTo("env1 child");
    }

    @Test
    public void should_resolve_a_part_of_the_name_of_the_parent_iteration_from_context_put_but_keep_the_var_from_dataset_unevaluated() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/forEachStrategy/iteration_strategy_with_variable_in_name_from_context_put_and_datset.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result).hasFieldOrPropertyWithValue("status", SUCCESS);
        assertThat(result.steps).hasSize(2);
        assertThat(result.steps.get(1).name).isEqualTo("Step 2 Parent : value ${#env}");
        assertThat(result.steps.get(1).steps).hasSize(2);
        assertThat(result.steps.get(1).steps.get(0).name).isEqualTo("Step 2 Parent : value env0");
        assertThat(result.steps.get(1).steps.get(1).name).isEqualTo("Step 2 Parent : value env1");
    }

    @Test
    public void should_resolve_a_part_of_the_name_of_the_child_iteration_from_context_put_but_keep_the_var_from_dataset_unevaluated() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/forEachStrategy/final_step_iteration_strategy_with_variable_in_name_from_context_put_and_datset.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result).hasFieldOrPropertyWithValue("status", SUCCESS);
        assertThat(result.steps).hasSize(2);
        assertThat(result.steps.get(1).name).isEqualTo("Step 2 : value ${#env}");
        assertThat(result.steps.get(1).steps).hasSize(2);
        assertThat(result.steps.get(1).steps.get(0).name).isEqualTo("Step 2 : value env0");
        assertThat(result.steps.get(1).steps.get(1).name).isEqualTo("Step 2 : value env1");
    }

    @Test
    public void should_repeat_step_with_iteration_strategy() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/forEachStrategy/simple_step_iterations.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result.steps.get(0).steps).hasSize(2);
        assertThat(result.status).isEqualTo(SUCCESS);
    }

    @Test
    public void should_evaluate_dataset_before_iterations() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/forEachStrategy/step_iteration_evaluated_dataset.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        StepExecutionReportDto parentStep = result.steps.get(0);
        assertThat(parentStep.steps).hasSize(2);
        assertThat(parentStep.status).isEqualTo(SUCCESS);

        StepExecutionReportDto iteration0 = parentStep.steps.get(0);
        assertThat(iteration0.status).isEqualTo(SUCCESS);
        assertThat(iteration0.name).startsWith("0 -");
        assertThat(iteration0.information.get(0)).isEqualTo("Validation [check_0_ok : ${#check_0 == \"/\" + #generatedID + \"/0\"}] : OK");
        assertDoesNotThrow(() -> UUID.fromString((String) iteration0.context.evaluatedInputs.get("stringParam")));

        StepExecutionReportDto iteration1 = parentStep.steps.get(1);
        assertThat(iteration1.status).isEqualTo(SUCCESS);
        assertThat(iteration1.name).startsWith("1 -");
        assertThat(iteration1.information.get(0)).isEqualTo("Validation [check_1_ok : ${#check_1 == \"/\" + #generatedID + \"/1\"}] : OK");
        assertDoesNotThrow(() -> UUID.fromString((String) iteration1.context.evaluatedInputs.get("stringParam")));
    }

    @Test
    public void should_preserve_input_types() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/forEachStrategy/step_iteration_preserve_input_types.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        StepExecutionReportDto iterationWithMap = result.steps.get(0);
        assertThat(iterationWithMap.steps).hasSize(2);
        assertThat(iterationWithMap.status).isEqualTo(SUCCESS);
        assertThat(((HashMap<?, ?>) iterationWithMap.steps.get(0).context.evaluatedInputs.values().stream().toList().get(0)).values())
            .hasExactlyElementsOfTypes(ZonedDateTime.class);

        StepExecutionReportDto iterationWithList = result.steps.get(1);
        assertThat(iterationWithList.steps).hasSize(2);
        assertThat(iterationWithList.status).isEqualTo(SUCCESS);
        assertThat(((ArrayList<?>) iterationWithList.steps.get(0).context.evaluatedInputs.values().stream().toList().get(0)).get(0))
            .isOfAnyClassIn(ZonedDateTime.class);
    }

    @Test
    public void should_fail_at_end_by_default() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/forEachStrategy/simple_step_iterations_fail_at_end.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        StepExecutionReportDto parentStep = result.steps.get(0);
        assertThat(parentStep.steps).hasSize(2);
        assertThat(parentStep.steps.get(0).name).startsWith("0 -");
        assertThat(parentStep.steps.get(0).errors).contains("Validation [check_0_ok : ${#env == \"B\"}] : KO");
        assertThat(parentStep.steps.get(0).status).isEqualTo(FAILURE);
        assertThat(parentStep.steps.get(1).name).startsWith("1 -");
        assertThat(parentStep.steps.get(1).information).contains("Validation [check_1_ok : ${#env == \"B\"}] : OK");
        assertThat(parentStep.steps.get(1).status).isEqualTo(SUCCESS);
    }

    @Test
    public void should_repeat_step_with_iteration_strategy_and_data_from_context() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/forEachStrategy/step_iterations_using_data_from_context.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result.steps.get(1).steps).hasSize(2);
        assertThat(result.status).isEqualTo(SUCCESS);
    }

    @Test
    public void should_iterate_within_parent_step_and_keep_substep_own_strategy() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/forEachStrategy/step_iterations_with_parent_and_substeps.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result.status).isEqualTo(FAILURE);
        StepExecutionReportDto iterativeStep = result.steps.get(0);
        assertThat(iterativeStep.steps).hasSize(3); // 3 iterations from dataset
        StepExecutionReportDto firstIteration = iterativeStep.steps.get(0);
        assertThat(firstIteration.steps).hasSize(1); // each iteration has only 1 step having a soft-assert strategy
        List<StepExecutionReportDto> softStep = firstIteration.steps.get(0).steps;
        assertThat(softStep).hasSize(2); // soft-assert strategy step has 2 children
        assertThat(softStep.get(0).status).isEqualTo(FAILURE);
        assertThat(softStep.get(1).status).isEqualTo(SUCCESS);
    }

    @Test
    public void should_accept_nested_loops_and_override_dataset() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/forEachStrategy/step_nested_iterations_with_overridden_dataset.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // Then the scenario succeed
        assertThat(result.status).isEqualTo(SUCCESS);

        // And the parent step has 2 iterations
        StepExecutionReportDto parentIterativeStep = result.steps.get(0);
        assertThat(parentIterativeStep.steps).hasSize(2); // has 2 iterations

        // And the first iteration contains 2 nested iterations
        StepExecutionReportDto firstIteration = parentIterativeStep.steps.get(0).steps.get(0);
        assertThat(firstIteration.steps).hasSize(2);
        assertThat(firstIteration.steps.get(0).name).startsWith("0 -");
        assertThat(firstIteration.steps.get(0).context.stepResults).containsEntry("environment_0.0","overriddenEnvX");
        assertThat(firstIteration.steps.get(1).name).startsWith("1 -");
        assertThat(firstIteration.steps.get(1).context.stepResults).containsEntry("environment_0.1","overriddenEnvY");

        // And the second iteration contains 2 nested iterations
        StepExecutionReportDto secondIteration = parentIterativeStep.steps.get(1).steps.get(0);
        assertThat(secondIteration.steps).hasSize(2);
        assertThat(secondIteration.steps.get(0).name).startsWith("0 -");
        assertThat(secondIteration.steps.get(0).context.stepResults).containsEntry("environment_1.0","overriddenEnvX");
        assertThat(secondIteration.steps.get(1).name).startsWith("1 -");
        assertThat(secondIteration.steps.get(1).context.stepResults).containsEntry("environment_1.1","overriddenEnvY");
    }

    @Test
    public void should_accept_nested_loops_with_different_dataset() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/forEachStrategy/step_nested_iterations_with_extended_dataset.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        StepExecutionReportDto parentIterativeStep = result.steps.get(0);
        assertThat(parentIterativeStep.steps).hasSize(2); // has 2 iterations
        assertThat(result.status).isEqualTo(SUCCESS);
    }

    @Test
    public void should_accept_double_nested_loops_with_different_dataset() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/forEachStrategy/step_double_nested_iterations_with_different_dataset.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result).hasFieldOrPropertyWithValue("status", SUCCESS);
        assertThat(result.name).isEqualTo("Test iterations");
        assertThat(result.steps).hasSize(1);
        assertThat(result.steps.get(0).name).isEqualTo("<i> - level 1 - ${#level1}");
        assertThat(result.steps.get(0).steps).hasSize(2);
        assertThat(result.steps.get(0).steps.get(0).name).isEqualTo("0 - level 1 - level1.0");
        assertThat(result.steps.get(0).steps.get(1).name).isEqualTo("1 - level 1 - level1.1");
        assertThat(result.steps.get(0).steps.get(0).steps).hasSize(1);
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).name).isEqualTo("0 <j> - level 2 - level1.0 ${#level2}");
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).steps).hasSize(2);
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).steps.get(0).name).isEqualTo("0 0 - level 2 - level1.0 level2.0");
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).steps.get(1).name).isEqualTo("0 1 - level 2 - level1.0 level2.1");
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).steps.get(0).steps).hasSize(1);
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).steps.get(0).steps.get(0).name).isEqualTo("0 0 <k> - level 3 - level1.0 level2.0 ${#level3}");
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).steps.get(0).steps.get(0).steps).hasSize(2);
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).steps.get(0).steps.get(0).steps.get(0).name).isEqualTo("0 0 0 - level 3 - level1.0 level2.0 level3.0");
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).steps.get(0).steps.get(0).steps.get(1).name).isEqualTo("0 0 1 - level 3 - level1.0 level2.0 level3.1");
    }
}
