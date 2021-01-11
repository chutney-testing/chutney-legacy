package com.chutneytesting.engine.domain.execution.engine.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.engine.domain.delegation.RemoteStepExecutor;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.RxBus;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.action.PauseExecutionAction;
import com.chutneytesting.engine.domain.execution.action.ResumeExecutionAction;
import com.chutneytesting.engine.domain.execution.action.StopExecutionAction;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContextImpl;
import com.chutneytesting.engine.domain.execution.evaluation.SpelFunctions;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReportBuilder;
import com.chutneytesting.engine.infrastructure.delegation.HttpClient;
import com.chutneytesting.task.spi.injectable.Target;
import io.reactivex.schedulers.Schedulers;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class StepTest {

    private StepDataEvaluator dataEvaluator = new StepDataEvaluator(new SpelFunctions());
    private Target fakeTarget = TargetImpl.NONE;
    private String environment = "";

    @Test
    public void stop_should_not_execute_test() {
        StepExecutor stepExecutor = mock(StepExecutor.class);
        doThrow(new RuntimeException()).when(stepExecutor).execute(any(), any(), any(), any());
        Step step = buildEmptyStep(stepExecutor);

        ScenarioExecution execution = ScenarioExecution.createScenarioExecution();
        RxBus.getInstance().post(new StopExecutionAction(execution.executionId));
        Status result = step.execute(execution, new ScenarioContextImpl());

        assertThat(result).isEqualTo(Status.STOPPED);
    }

    @Test
    public void pause_should_pause_test() {
        StepExecutor stepExecutor = mock(StepExecutor.class);
        Step step = buildEmptyStep(stepExecutor);

        ScenarioExecution execution = ScenarioExecution.createScenarioExecution();

        RxBus.getInstance().post(new PauseExecutionAction(execution.executionId));
        Schedulers.io().createWorker().schedule(() -> step.execute(execution, new ScenarioContextImpl()));
        waitMs(1100);
        verify(stepExecutor, times(0)).execute(any(), any(), any(), any());

        RxBus.getInstance().post(new ResumeExecutionAction(execution.executionId));
        waitMs(1100);
        verify(stepExecutor, times(1)).execute(any(), any(), any(), any());
    }

    @Test
    public void should_have_output_of_step_store_in_step_result() {
        // Given
        StepExecutor stepExecutor = mock(StepExecutor.class);

        Map<String, Object> outputs = new HashMap<>();
        outputs.put("aValue", "42");
        outputs.put("anotherValue", "43");

        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "taskType", null, null, null, outputs,environment);
        Step step = new Step(dataEvaluator, fakeStepDefinition, Optional.empty(), stepExecutor, Lists.emptyList());
        ScenarioContextImpl scenarioContext = new ScenarioContextImpl();

        // When
        step.execute(ScenarioExecution.createScenarioExecution(), scenarioContext);

        // Then
        StepContext context = (StepContext) ReflectionTestUtils.getField(step, "stepContext");
        assertThat(context.getStepOutputs().get("aValue")).isEqualTo("42");
        assertThat(context.getStepOutputs().get("anotherValue")).isEqualTo("43");
    }

    @Test
    public void values_already_set_in_scenario_context_can_be_updated_and_override_by_each_step_execution() {
        // Given
        Map<String, Object> existingResult = new HashMap<>();
        existingResult.put("aValueToEvaluate", 500);
        existingResult.put("anotherValueToEvaluate", "{ new value }");

        StepExecutor stepExecutor = mock(StepExecutor.class);
        doAnswer(
            iom -> {
                StepContext context = iom.getArgument(1);
                context.addScenarioContext(existingResult);
                return null;
            })
            .when(stepExecutor).execute(any(), any(), any(), any());


        /* These outputs should override existing values in scenario context */
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("aValue", "${#aValueToEvaluate}");
        outputs.put("anotherValue", "${#anotherValueToEvaluate}");

        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "taskType", null, null, null, outputs,environment);
        Step step = new Step(dataEvaluator, fakeStepDefinition, Optional.empty(), stepExecutor, Lists.emptyList());
        ScenarioContextImpl scenarioContext = new ScenarioContextImpl();

        // When
        step.execute(ScenarioExecution.createScenarioExecution(), scenarioContext);

        // Then
        StepContext context = (StepContext) ReflectionTestUtils.getField(step, "stepContext");
        assertThat(context.getScenarioContext().get("aValue")).as("New value").isEqualTo(500);
        assertThat(context.getScenarioContext().get("anotherValue")).as("New value").isEqualTo("{ new value }");
        assertThat(context.getStepOutputs().get("aValue")).as("New value").isEqualTo(500);
        assertThat(context.getStepOutputs().get("anotherValue")).as("New value").isEqualTo("{ new value }");
    }

    @Test
    public void context_is_update_with_values_from_a_remote_execution() {

        // Given
        Map<String, Object> fakeStepResults = new HashMap<>();
        fakeStepResults.put("aResultKeySetByATask", 4242);
        fakeStepResults.put("anotherResultKeySetByATask", "{ a value as string }");

        //  This scenario will be executed by a faked delegate agent which returns stepResults
        HttpClient mockHttpClient = mock(HttpClient.class);
        StepExecutionReport fakeRemoteReport = new StepExecutionReportBuilder().setName("fake")
            .setDuration(42L)
            .setStartDate(Instant.now())
            .setStatus(Status.SUCCESS)
            .setStepResults(fakeStepResults)
            .createStepExecutionReport();
        when(mockHttpClient.handDown(any(), any()))
            .thenReturn(fakeRemoteReport);

        StepExecutor fakeRemoteStepExecutor = new RemoteStepExecutor(mockHttpClient, mock(NamedHostAndPort.class));

        // These outputs should override existing values in scenario context
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("anAliasForReuse", "${#aResultKeySetByATask}");
        outputs.put("anotherAliasForReuse", "${#anotherResultKeySetByATask}");

        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "taskType", null, null, null, outputs,environment);
        Step step = new Step(dataEvaluator, fakeStepDefinition, Optional.empty(), fakeRemoteStepExecutor, Lists.emptyList());
        ScenarioContextImpl scenarioContext = new ScenarioContextImpl();

        // When
        step.execute(ScenarioExecution.createScenarioExecution(), scenarioContext);

        // Then
        StepContext context = (StepContext) ReflectionTestUtils.getField(step, "stepContext");
        assertThat(context.getScenarioContext().get("anAliasForReuse")).as("New value from Remote").isEqualTo(4242);
        assertThat(context.getScenarioContext().get("anotherAliasForReuse")).as("New value from Remote").isEqualTo("{ a value as string }");
        assertThat(context.getStepOutputs().get("anAliasForReuse")).as("New value from Remote").isEqualTo(4242);
        assertThat(context.getStepOutputs().get("anotherAliasForReuse")).as("New value from Remote").isEqualTo("{ a value as string }");

    }

    @Test
    public void target_and_environment_are_set_in_scenario_context_in_order_to_be_used_by_evaluated_inputs() {
        // Given
        TargetImpl fakeTarget = TargetImpl.builder().withName("fakeTargetName").build();
        String environment = "FakeTestEnvironment";

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("targetName", "${#target.name}");
        inputs.put("currentEnvironment", "${#environment}");

        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", this.fakeTarget, "taskType", null, inputs, null, null,environment);
        Step step = new Step(dataEvaluator, fakeStepDefinition, Optional.of(fakeTarget), mock(StepExecutor.class), Lists.emptyList());

        // When
        step.execute(ScenarioExecution.createScenarioExecution(), new ScenarioContextImpl());

        // Then
        StepContext context = (StepContext) ReflectionTestUtils.getField(step, "stepContext");
        assertThat(context.getEvaluatedInputs()).hasSize(2);
        assertThat(context.getEvaluatedInputs()).containsKeys("targetName");
        assertThat(context.getEvaluatedInputs().get("targetName")).isEqualTo("fakeTargetName");
        assertThat(context.getEvaluatedInputs()).containsKeys("currentEnvironment");
        assertThat(context.getEvaluatedInputs().get("currentEnvironment")).isEqualTo(environment);
    }

    @Test
    public void local_status_is_updated_from_the_status_of_a_remote_execution() {
        // When
        Status scenarioOK = executeWithRemote(Status.SUCCESS);
        Status scenarioKO = executeWithRemote(Status.FAILURE);

        // Then
        assertThat(scenarioOK).as("Local status").isEqualTo(Status.SUCCESS);
        assertThat(scenarioKO).as("Local status").isEqualTo(Status.FAILURE);
    }

    @Test
    public void should_manage_execution_time_watch() {
        StepExecutor stepExecutor = mock(StepExecutor.class);
        Step step = buildEmptyStep(stepExecutor);

        step.startWatch();
        waitMs(100);

        long duration = step.duration().toMillis();
        assertThat(duration).isPositive();

        waitMs(100);
        step.stopWatch();
        long durationAfterStop = step.duration().toMillis();

        assertThat(durationAfterStop).isGreaterThan(duration);

        waitMs(100);
        assertThat(step.duration().toMillis()).isEqualTo(durationAfterStop);

        step.startWatch();
        waitMs(100);
        assertThat(step.duration().toMillis()).isGreaterThan(durationAfterStop);
    }

    @Test
    public void should_not_compute_substeps_status_if_current_status_is_failure() {
        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "taskType", null, null, null, null,environment);
        Step step = new Step(dataEvaluator, fakeStepDefinition, Optional.empty(), mock(StepExecutor.class), Lists.list(mock(Step.class), mock(Step.class)));
        step.failure("...");
        assertThat(step.status()).isEqualTo(Status.FAILURE);

        Status status = step.status();

        verify(step.subSteps().get(0), times(0)).status();
        verify(step.subSteps().get(1), times(0)).status();
        assertThat(status).isEqualTo(step.status());
    }

    private Status executeWithRemote(Status remoteStatus) {

        HttpClient mockHttpClient = mock(HttpClient.class);
        StepExecutionReport fakeRemoteReport = new StepExecutionReportBuilder()
            .setName("fake")
            .setDuration(42L)
            .setStatus(remoteStatus)
            .createStepExecutionReport();
        when(mockHttpClient.handDown(any(), any()))
            .thenReturn(fakeRemoteReport);

        StepExecutor fakeRemoteStepExecutor = new RemoteStepExecutor(mockHttpClient, mock(NamedHostAndPort.class));

        Step step = buildEmptyStep(fakeRemoteStepExecutor);
        ScenarioContextImpl scenarioContext = new ScenarioContextImpl();

        // When
        return step.execute(ScenarioExecution.createScenarioExecution(), scenarioContext);
    }

    private Step buildEmptyStep(StepExecutor stepExecutor) {
        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "taskType", null, null, null, null,environment);
        return new Step(dataEvaluator, fakeStepDefinition, Optional.empty(), stepExecutor, Lists.emptyList());
    }

    private void waitMs(long msToWaitFor) {
        try {
            TimeUnit.MILLISECONDS.sleep(msToWaitFor);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
