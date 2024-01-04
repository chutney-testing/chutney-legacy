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

import static com.chutneytesting.engine.api.execution.StatusDto.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.chutneytesting.ExecutionConfiguration;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.StatusDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.engine.api.execution.TestEngine;
import com.chutneytesting.tools.Jsons;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StepIterationStrategyTest {

    @Test
    public void should_fail_if_no_dataset_with_iteration_strategy() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/error_without_dataset_iterations.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result).hasFieldOrPropertyWithValue("status", StatusDto.FAILURE);
    }

    @Test
    public void should_resolve_name_from_context_with_iteration_strategy() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/iteration_strategy_step_with_name_resolver_from_context_put.json", ExecutionRequestDto.class);

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
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/iteration_strategy_using_idx_in_sub_sub_step.json", ExecutionRequestDto.class);

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
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/iteration_strategy_using_value_in_sub_sub_sub_step.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result).hasFieldOrPropertyWithValue("status", SUCCESS);
        assertThat(result.name).isEqualTo("${#env} - parent");
        assertThat(result.steps).hasSize(1);
        assertThat(result.steps.get(0).name).isEqualTo("env0 - parent");
        assertThat(result.steps.get(0).steps).hasSize(1);
        assertThat(result.steps.get(0).steps.get(0).name).isEqualTo("env0 sub parent");
        assertThat(result.steps.get(0).steps.get(0).steps).hasSize(1);
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).name).isEqualTo("env0 sub sub parent");
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).steps).hasSize(1);
        assertThat(result.steps.get(0).steps.get(0).steps.get(0).steps.get(0).name).isEqualTo("env0 child");
    }

    @Test
    public void should_resolve_a_part_of_the_name_of_the_parent_iteration_from_context_put_but_keep_the_var_from_dataset_unevaluated() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/iteration_strategy_with_variable_in_name_from_context_put_and_datset.json", ExecutionRequestDto.class);

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
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/final_step_iteration_strategy_with_variable_in_name_from_context_put_and_datset.json", ExecutionRequestDto.class);

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
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/simple_step_iterations.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result.steps.get(0).steps).hasSize(2);
        assertThat(result).hasFieldOrPropertyWithValue("status", SUCCESS);
    }

    @Test
    public void should_evaluate_dataset_before_iterations() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/step_iteration_evaluated_dataset.json", ExecutionRequestDto.class);

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
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/step_iteration_preserve_input_types.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        StepExecutionReportDto parentStep = result.steps.get(0);
        assertThat(parentStep.steps).hasSize(2);
        assertThat(parentStep.status).isEqualTo(SUCCESS);

        parentStep = result.steps.get(1);
        assertThat(parentStep.steps).hasSize(2);
        assertThat(parentStep.status).isEqualTo(SUCCESS);
    }

    @Test
    public void should_fail_at_end_by_default() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/simple_step_iterations_fail_at_end.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        StepExecutionReportDto parentStep = result.steps.get(0);
        assertThat(parentStep.steps).hasSize(2);
        assertThat(parentStep.steps.get(0).name).isEqualTo("0 - Hello website on A with user Tata");
        assertThat(parentStep.steps.get(0).errors).contains("Validation [check_0_ok : ${#env == \"B\"}] : KO");
        assertThat(parentStep.steps.get(0).status).isEqualTo(StatusDto.FAILURE);
        assertThat(parentStep.steps.get(1).name).isEqualTo("1 - Hello website on B with user Baba");
        assertThat(parentStep.steps.get(1).information).contains("Validation [check_1_ok : ${#env == \"B\"}] : OK");
        assertThat(parentStep.steps.get(1).status).isEqualTo(SUCCESS);
    }

    @Test
    public void should_repeat_step_with_iteration_strategy_and_data_from_context() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/step_iterations_using_data_from_context.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result.steps.get(1).steps).hasSize(2);
        assertThat(result).hasFieldOrPropertyWithValue("status", SUCCESS);
    }

    @Test
    public void should_iterate_within_parent_step_and_keep_substep_own_strategy() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/step_iterations_with_parent_and_substeps.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result).hasFieldOrPropertyWithValue("status", StatusDto.FAILURE);
        StepExecutionReportDto iterativeStep = result.steps.get(0);
        assertThat(iterativeStep.steps).hasSize(3); // 3 iterations from dataset
        StepExecutionReportDto firstIteration = iterativeStep.steps.get(0);
        assertThat(firstIteration.steps).hasSize(1); // each iteration has only 1 step having a soft-assert strategy
        List<StepExecutionReportDto> softStep = firstIteration.steps.get(0).steps;
        assertThat(softStep).hasSize(2); // soft-assert strategy step has 2 children
        assertThat(softStep.get(0).status).isEqualTo(StatusDto.FAILURE);
        assertThat(softStep.get(1).status).isEqualTo(SUCCESS);
    }

}
