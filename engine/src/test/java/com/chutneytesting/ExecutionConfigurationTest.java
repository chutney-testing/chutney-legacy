package com.chutneytesting;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto.StepDefinitionRequestDto;
import com.chutneytesting.engine.api.execution.StatusDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.engine.api.execution.TestEngine;
import com.chutneytesting.task.domain.TaskTemplate;
import com.chutneytesting.task.domain.TaskTemplateParserV2;
import com.chutneytesting.task.domain.TaskTemplateRegistry;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.assertj.core.util.Maps;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class ExecutionConfigurationTest {

    private ExecutionConfiguration sut = new ExecutionConfiguration();

    @Test
    public void should_load_configuration_without_error() {
        assertThat(sut).isNotNull();
//        assertThat(applicationContext.getBean(TaskTemplateLoader.class)).isNotNull();
//        assertThat(applicationContext.getBean(TaskTemplateRegistry.class)).isNotNull();
//        assertThat(applicationContext.getBean(SpelFunctions.class)).isNotNull();
//        assertThat(applicationContext.getBean("stepExecutionStrategies")).isNotNull();
//        assertThat(applicationContext.getBean(StepExecutionStrategies.class)).isNotNull();
//        assertThat(applicationContext.getBean(StepDataEvaluator.class)).isNotNull();
//        assertThat(applicationContext.getBean(ExecutionEngine.class)).isNotNull();
//        assertThat(applicationContext.getBean(TestEngine.class)).isNotNull();
//        assertThat(applicationContext.getBean(DelegationService.class)).isNotNull();
//        assertThat(applicationContext.getBean(DelegationClient.class)).isNotNull();
//        assertThat(applicationContext.getBean(Reporter.class)).isNotNull();
//        assertThat(applicationContext.getBean(ExecutionManager.class)).isNotNull();
//        assertThat(applicationContext.getBean(StepExecutor.class)).isNotNull();
    }

    @Test
    public void should_execute_scenario_async() {
        //G
        final TestEngine testEngine = sut.embeddedTestEngine();
        StepDefinitionRequestDto stepDefinition = createSucessStep();
        ExecutionRequestDto requestDto = new ExecutionRequestDto(stepDefinition);

        //W
        Long executionId = testEngine.executeAsync(requestDto);
        Observable<StepExecutionReportDto> reports = testEngine.receiveNotification(executionId);
        List<StepExecutionReportDto> results = new ArrayList<>();
        reports.blockingSubscribe(results::add);

        //T
        assertThat(results.get(results.size() - 1)).hasFieldOrPropertyWithValue("status", StatusDto.SUCCESS);
    }

    @Test
    public void should_execute_scenario_sync() {
        //G
        final TestEngine testEngine = sut.embeddedTestEngine();
        StepDefinitionRequestDto stepDefinition = createSucessStep();
        ExecutionRequestDto requestDto = new ExecutionRequestDto(stepDefinition);

        //W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        //T
        assertThat(result).hasFieldOrPropertyWithValue("status", StatusDto.SUCCESS);
    }

    @Test
    public void should_pause_resume_stop_scenario() {
        //G
        final TestEngine testEngine = sut.embeddedTestEngine();
        StepDefinitionRequestDto stepDefinition = createScenarioForPause();
        ExecutionRequestDto requestDto = new ExecutionRequestDto(stepDefinition);

        //W
        Long executionId = testEngine.executeAsync(requestDto);
        Observable<StepExecutionReportDto> reports = testEngine.receiveNotification(executionId);
        List<StepExecutionReportDto> results = new ArrayList<>();
        reports.subscribeOn(Schedulers.io()).subscribe(results::add);

        sleep(200); // wait initialization and execution of the first step

        testEngine.pauseExecution(executionId);
        sleep(800); // wait pause of 1 second (@See ScenarioExecution)

        // Resume before next pause
        testEngine.resumeExecution(executionId);
        sleep(600);
        testEngine.stopExecution(executionId);
        sleep(600);


        StepExecutionReportDto finalReport = results.get(results.size() - 1);
        // check scenario status
        assertThat(finalReport).hasFieldOrPropertyWithValue("status", StatusDto.STOPPED);
        // check first step status
        assertThat(finalReport.steps.get(0)).hasFieldOrPropertyWithValue("status", StatusDto.SUCCESS);
        // check second step status
        assertThat(finalReport.steps.get(1)).hasFieldOrPropertyWithValue("status", StatusDto.SUCCESS);
        // check third step status
        assertThat(finalReport.steps.get(2)).hasFieldOrPropertyWithValue("status", StatusDto.STOPPED);
    }

    @Test
    public void should_catch_exception_in_fault_barrier_engine() {
        //G
        final TestEngine testEngine = sut.embeddedTestEngine();
        final TaskTemplateRegistry taskTemplateRegistry = sut.taskTemplateRegistry();
        TaskTemplate taskTemplate = new TaskTemplateParserV2().parse(ErrorTask.class).result();
        Map<String, TaskTemplate> taskTemplatesByType = (Map<String, TaskTemplate>) ReflectionTestUtils.getField(taskTemplateRegistry, "taskTemplatesByType");
        taskTemplatesByType.put("error", taskTemplate);

        StepDefinitionRequestDto stepDefinition = new StepDefinitionRequestDto(
            "throw runtime exception step",
            null,
            null,
            "error",
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyMap(),
            ""
        );
        ExecutionRequestDto requestDto = new ExecutionRequestDto(stepDefinition);

        //W
        StepExecutionReportDto result = testEngine.execute(requestDto);

        //T
        assertThat(result).hasFieldOrPropertyWithValue("status", StatusDto.FAILURE);
        assertThat(result.errors.get(0)).isEqualTo("Task [error] failed: Should be catch by fault barrier");
    }

    private StepDefinitionRequestDto createSucessStep() {
        return new StepDefinitionRequestDto(
                  "scenario name",
                  null,
            null,
                  "success",
                  Collections.emptyMap(),
                  Collections.emptyList(),
                  Collections.emptyMap(),
            ""
              );
    }

    private StepDefinitionRequestDto createScenarioForPause() {
        List<StepDefinitionRequestDto> steps = new ArrayList<>();
        steps.add(createSleepsStep("sleep 1"));
        steps.add(createSleepsStep("sleep 2"));
        steps.add(createSleepsStep("sleep 3"));
        return new StepDefinitionRequestDto(
            "scenario name",
            null,
            null,
            null,
            Collections.emptyMap(),
            steps,
            Collections.emptyMap(),
            ""
        );
    }

    private StepDefinitionRequestDto createSleepsStep(String name) {
        return new StepDefinitionRequestDto(
            name,
            null,
            null,
            "sleep",
            Maps.newHashMap("duration", "500 ms"),
            null,
            Collections.emptyMap(),
            ""
        );
    }


    private void sleep(long time) {
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public static class ErrorTask implements Task {

        public ErrorTask() {
        }

        @Override
        public TaskExecutionResult execute() {
            throw new RuntimeException("Should be catch by fault barrier");
        }
    }
}
