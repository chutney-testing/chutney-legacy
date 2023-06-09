package com.chutneytesting.engine.domain.execution.strategies;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.ExecutionConfiguration;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.StatusDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.engine.api.execution.TestEngine;
import com.chutneytesting.tools.Jsons;
import java.util.List;
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
    public void should_repeat_step_with_iteration_strategy() {
        // G
        final TestEngine testEngine = new ExecutionConfiguration().embeddedTestEngine();
        ExecutionRequestDto requestDto = Jsons.loadJsonFromClasspath("scenarios_examples/simple_step_iterations.json", ExecutionRequestDto.class);

        // W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        // T
        assertThat(result.steps.get(0).steps).hasSize(2);
        assertThat(result).hasFieldOrPropertyWithValue("status", StatusDto.SUCCESS);
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
        assertThat(parentStep.steps.get(0).errors).contains("Validation [pouet_0_ok : ${#env == \"B\"}] : KO");
        assertThat(parentStep.steps.get(0).status).isEqualTo(StatusDto.FAILURE);
        assertThat(parentStep.steps.get(1).name).isEqualTo("1 - Hello website on B with user Baba");
        assertThat(parentStep.steps.get(1).information).contains("Validation [pouet_1_ok : ${#env == \"B\"}] : OK");
        assertThat(parentStep.steps.get(1).status).isEqualTo(StatusDto.SUCCESS);
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
        assertThat(result).hasFieldOrPropertyWithValue("status", StatusDto.SUCCESS);
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
        assertThat(softStep.get(1).status).isEqualTo(StatusDto.SUCCESS);
    }

}
