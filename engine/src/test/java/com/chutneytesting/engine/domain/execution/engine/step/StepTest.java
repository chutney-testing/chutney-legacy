package com.chutneytesting.engine.domain.execution.engine.step;

import static com.chutneytesting.tools.WaitUtils.awaitDuring;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.engine.domain.delegation.RemoteStepExecutor;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.RxBus;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.command.PauseExecutionCommand;
import com.chutneytesting.engine.domain.execution.command.ResumeExecutionCommand;
import com.chutneytesting.engine.domain.execution.command.StopExecutionCommand;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContextImpl;
import com.chutneytesting.engine.domain.execution.evaluation.SpelFunctions;
import com.chutneytesting.engine.domain.execution.event.EndStepExecutionEvent;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReportBuilder;
import com.chutneytesting.engine.infrastructure.delegation.HttpClient;
import io.reactivex.schedulers.Schedulers;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

public class StepTest {

    private final StepDataEvaluator dataEvaluator = new StepDataEvaluator(new SpelFunctions());
    private final Target fakeTarget = TargetImpl.NONE;
    private final String environment = "";

    @Test
    public void stop_should_not_execute_test() {
        StepExecutor stepExecutor = mock(StepExecutor.class);
        doThrow(new RuntimeException()).when(stepExecutor).execute(any(), any(), any());
        Step step = buildEmptyStep(stepExecutor);

        ScenarioExecution execution = ScenarioExecution.createScenarioExecution(null);
        awaitDuring(500, MILLISECONDS);
        RxBus.getInstance().post(new StopExecutionCommand(execution.executionId));
        await().atMost(5, SECONDS).untilAsserted(() -> assertThat(execution.hasToStop()).isTrue());

        Status result = step.execute(execution, new ScenarioContextImpl());
        assertThat(result).isEqualTo(Status.STOPPED);
    }

    @Test
    public void pause_should_pause_test() {
        StepExecutor stepExecutor = mock(StepExecutor.class);
        Step step = buildEmptyStep(stepExecutor);

        ScenarioExecution execution = ScenarioExecution.createScenarioExecution(null);

        RxBus.getInstance().registerOnExecutionId(PauseExecutionCommand.class, execution.executionId, e -> {
            Schedulers.io().createWorker().schedule(() -> step.execute(execution, new ScenarioContextImpl()));
            await().atMost(1100, MILLISECONDS).untilAsserted(() ->
                verify(stepExecutor, times(0)).execute(any(), any(), any())
            );
        });
        RxBus.getInstance().post(new PauseExecutionCommand(execution.executionId));

        RxBus.getInstance().registerOnExecutionId(ResumeExecutionCommand.class, execution.executionId, e -> {
            await().atMost(1100, MILLISECONDS).untilAsserted(() ->
                verify(stepExecutor, times(1)).execute(any(), any(), any())
            );
        });
        RxBus.getInstance().post(new ResumeExecutionCommand(execution.executionId));
    }

    @Test
    public void should_have_output_of_step_store_in_step_result() {
        // Given
        StepExecutor stepExecutor = new FakeStepExecutor(ActionExecutionResult.ok());

        Map<String, Object> outputs = new HashMap<>();
        outputs.put("aValue", "42");
        outputs.put("anotherValue", "43");

        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "actionType", null, null, null, outputs, null, environment);
        Step step = new Step(dataEvaluator, fakeStepDefinition, stepExecutor, emptyList());
        ScenarioContextImpl scenarioContext = new ScenarioContextImpl();

        // When
        step.execute(ScenarioExecution.createScenarioExecution(null), scenarioContext);

        // Then
        assertThat(step.getStepOutputs().get("aValue")).isEqualTo("42");
        assertThat(step.getStepOutputs().get("anotherValue")).isEqualTo("43");
    }

    @Test
    public void validations_should_inform_ok_ko_in_step_result() {
        // Given
        StepExecutor stepExecutor = new FakeStepExecutor(ActionExecutionResult.ok());

        Map<String, Object> validations = new HashMap<>();
        validations.put("first assert", "${false}");
        validations.put("second assert", "${true}");

        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "actionType", null, null, null, null, validations, environment);
        Step step = new Step(dataEvaluator, fakeStepDefinition, stepExecutor, emptyList());
        ScenarioContextImpl scenarioContext = new ScenarioContextImpl();

        // When
        step.execute(ScenarioExecution.createScenarioExecution(null), scenarioContext);

        // Then
        StepState state = (StepState) ReflectionTestUtils.getField(step, "state");
        assertThat(state.errors().size()).isEqualTo(1);
        assertThat(state.informations().size()).isEqualTo(1);
        assertThat(state.errors().get(0)).isEqualTo("Validation [first assert : ${false}] : KO");
        assertThat(state.informations().get(0)).isEqualTo("Validation [second assert : ${true}] : OK");
    }

    @Test
    public void validations_should_set_failure_state() {
        // Given
        StepExecutor stepExecutor = new FakeStepExecutor(ActionExecutionResult.ok());

        Map<String, Object> validations = new HashMap<>();
        validations.put("first assert", "${false}");
        validations.put("second assert", "${true}");

        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "actionType", null, null, null, null, validations, environment);
        Step step = new Step(dataEvaluator, fakeStepDefinition, stepExecutor, emptyList());
        ScenarioContextImpl scenarioContext = new ScenarioContextImpl();

        // When
        step.execute(ScenarioExecution.createScenarioExecution(null), scenarioContext);

        // Then
        assertThat(step.status()).isEqualTo(Status.FAILURE);
    }

    @Test
    public void values_already_set_in_scenario_context_can_be_updated_and_override_by_each_step_execution() {
        // Given
        Map<String, Object> existingResult = new HashMap<>();
        existingResult.put("aValueToEvaluate", 500);
        existingResult.put("anotherValueToEvaluate", "{ new value }");

        StepExecutor stepExecutor = new FakeStepExecutor(ActionExecutionResult.ok(existingResult));

        /* These outputs should override existing values in scenario context */
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("aValue", "${#aValueToEvaluate}");
        outputs.put("anotherValue", "${#anotherValueToEvaluate}");

        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "actionType", null, null, null, outputs, null, environment);
        Step step = new Step(dataEvaluator, fakeStepDefinition, stepExecutor, emptyList());
        ScenarioContextImpl scenarioContext = new ScenarioContextImpl();

        // When
        step.execute(ScenarioExecution.createScenarioExecution(null), scenarioContext);

        // Then
        assertThat(step.getScenarioContext().get("aValue")).as("New value").isEqualTo(500);
        assertThat(step.getScenarioContext().get("anotherValue")).as("New value").isEqualTo("{ new value }");
        assertThat(step.getStepOutputs().get("aValue")).as("New value").isEqualTo(500);
        assertThat(step.getStepOutputs().get("anotherValue")).as("New value").isEqualTo("{ new value }");
    }

    @Test
    public void context_is_update_with_values_from_a_remote_execution() {

        // Given
        Map<String, Object> fakeStepResults = new HashMap<>();
        fakeStepResults.put("aResultKeySetByAAction", 4242);
        fakeStepResults.put("anotherResultKeySetByAAction", "{ a value as string }");

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
        outputs.put("anAliasForReuse", "${#aResultKeySetByAAction}");
        outputs.put("anotherAliasForReuse", "${#anotherResultKeySetByAAction}");

        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "actionType", null, null, null, outputs, null, environment);
        Step step = new Step(dataEvaluator, fakeStepDefinition, fakeRemoteStepExecutor, emptyList());
        ScenarioContextImpl scenarioContext = new ScenarioContextImpl();

        // When
        step.execute(ScenarioExecution.createScenarioExecution(null), scenarioContext);

        // Then
        assertThat(step.getScenarioContext().get("anAliasForReuse")).as("New value from Remote").isEqualTo(4242);
        assertThat(step.getScenarioContext().get("anotherAliasForReuse")).as("New value from Remote").isEqualTo("{ a value as string }");
        assertThat(step.getStepOutputs().get("anAliasForReuse")).as("New value from Remote").isEqualTo(4242);
        assertThat(step.getStepOutputs().get("anotherAliasForReuse")).as("New value from Remote").isEqualTo("{ a value as string }");

    }

    @Test
    public void target_is_set_in_local_context_in_order_to_be_used_by_evaluated_inputs() {
        // Given
        TargetImpl fakeTarget = TargetImpl.builder().withName("fakeTargetName").build();

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("targetName", "${#target.name}");

        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "actionType", null, inputs, null, null, null, environment);
        Step step = new Step(dataEvaluator, fakeStepDefinition, mock(StepExecutor.class), emptyList());

        // When
        step.execute(ScenarioExecution.createScenarioExecution(null), new ScenarioContextImpl());

        // Then
        assertThat(step.getEvaluatedInputs()).hasSize(1);
        assertThat(step.getEvaluatedInputs()).containsKeys("targetName");
        assertThat(step.getEvaluatedInputs().get("targetName")).isEqualTo("fakeTargetName");
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
        awaitDuring(100, MILLISECONDS);

        long duration = step.duration().toMillis();
        assertThat(duration).isPositive();
        awaitDuring(100, MILLISECONDS);

        step.stopWatch();
        long durationAfterStop = step.duration().toMillis();

        assertThat(durationAfterStop).isGreaterThan(duration);
        awaitDuring(100, MILLISECONDS);

        assertThat(step.duration().toMillis()).isEqualTo(durationAfterStop);

        step.startWatch();

        awaitDuring(100, MILLISECONDS);
        assertThat(step.duration().toMillis()).isGreaterThan(durationAfterStop);
    }

    @Test
    public void should_not_compute_substeps_status_if_current_status_is_failure() {
        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "actionType", null, null, null, null, null, environment);
        Step step = new Step(dataEvaluator, fakeStepDefinition, mock(StepExecutor.class), Lists.list(mock(Step.class), mock(Step.class)));
        step.failure("...");
        assertThat(step.status()).isEqualTo(Status.FAILURE);

        Status status = step.status();

        verify(step.subSteps().get(0), times(0)).status();
        verify(step.subSteps().get(1), times(0)).status();
        assertThat(status).isEqualTo(step.status()); // Always true ? what was that for ?
    }

    @Test
    void should_not_run_validations_nor_evaluate_outputs_when_action_fails() {
        // Given
        StepExecutor stepExecutor = new FakeStepExecutor(ActionExecutionResult.ko());

        Map<String, Object> outputs = new HashMap<>();
        outputs.put("aValue", "${#validation_error}");
        outputs.put("anotherValue", "42");

        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "actionType", null, null, null, outputs, null, environment);
        Step step = new Step(dataEvaluator, fakeStepDefinition, stepExecutor, emptyList());
        ScenarioContextImpl scenarioContext = new ScenarioContextImpl();

        // When
        step.execute(ScenarioExecution.createScenarioExecution(null), scenarioContext);

        // Then
        assertThat(step.status()).isEqualTo(Status.FAILURE);
        assertThat(step.errors()).isEmpty(); // checking validations
        assertThat(step.getStepOutputs()).isEmpty();
    }

    @Test
    void should_not_evaluate_registred_final_action_inputs() {
        // Given
        String environment = "FakeTestEnvironment";

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("currentEnvironment", "${#environment}");
        inputs.put("validations", Map.of("validation_1", "${#validation}"));

        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "final", null, inputs, null, null, null, environment);
        Step step = new Step(dataEvaluator, fakeStepDefinition, mock(StepExecutor.class), emptyList());

        // When
        step.execute(ScenarioExecution.createScenarioExecution(null), new ScenarioContextImpl());

        // Then
        assertThat(step.getEvaluatedInputs()).hasSize(2);
        assertThat(step.getEvaluatedInputs().get("currentEnvironment")).isEqualTo("${#environment}");
        assertThat(step.getEvaluatedInputs()).containsKeys("validations");
        assertThat(((Map<?, ?>) step.getEvaluatedInputs().get("validations")).get("validation_1")).isEqualTo("${#validation}");

    }

    @Test
    public void should_evaluate_spel_for_target_data() {
        // Given
        TargetImpl fakeTarget = TargetImpl.builder()
            .withName("NAME")
            .withUrl("${#dynamicUrl}")
            .withProperties(Map.of("${#dynamicPropertiesKey}", "${#dynamicPropertiesValue}"))
            .build();
        String environment = "FakeTestEnvironment";

        ScenarioContextImpl scenarioContext = new ScenarioContextImpl();
        scenarioContext.put("dynamicUrl", "uri");
        scenarioContext.put("dynamicPropertiesKey", "key");
        scenarioContext.put("dynamicPropertiesValue", "value");

        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "taskType", null, emptyMap(), null, null, null, environment);
        StepExecutor stepExecutorMock = mock(StepExecutor.class);
        Step step = new Step(dataEvaluator, fakeStepDefinition, stepExecutorMock, emptyList());

        // When

        step.execute(ScenarioExecution.createScenarioExecution(null), scenarioContext);

        // Then
        ArgumentCaptor<Target> targetCaptor = ArgumentCaptor.forClass(Target.class);
        verify(stepExecutorMock).execute(any(), targetCaptor.capture(), any());

        Target target = targetCaptor.getValue();
        assertThat(target.name()).isEqualTo("NAME");
        assertThat(target.rawUri()).isEqualTo("uri");
        assertThat(target.property("key").get()).isEqualTo("value");
    }

    @ParameterizedTest
    @MethodSource("executionResults")
    void should_notify_execution_end_with_correct_state(ActionExecutionResult ter) {
        StepExecutor stepExecutor = new FakeStepExecutor(ter);
        Step step = buildEmptyStep(stepExecutor);

        ScenarioExecution execution = ScenarioExecution.createScenarioExecution(null);

        AtomicReference<Step> notifyStep = new AtomicReference<>();
        RxBus.getInstance().registerOnExecutionId(EndStepExecutionEvent.class, execution.executionId,
            e -> {
                EndStepExecutionEvent ee = (EndStepExecutionEvent) e;
                notifyStep.set(ee.step);
            });

        Status returnedStatus = step.execute(execution, new ScenarioContextImpl());
        Step notifiedStep = await().untilAtomic(notifyStep, Matchers.notNullValue(Step.class));

        assertThat(notifiedStep.status()).isEqualTo(returnedStatus);
        assertThat(System.identityHashCode(step)).isEqualTo(System.identityHashCode(notifiedStep));
    }

    private static Stream<Arguments> executionResults() {
        return Stream.of(
            Arguments.of(ActionExecutionResult.ok()),
            Arguments.of(ActionExecutionResult.ko())
        );
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
        return step.execute(ScenarioExecution.createScenarioExecution(null), scenarioContext);
    }

    private Step buildEmptyStep(StepExecutor stepExecutor) {
        StepDefinition fakeStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "actionType", null, null, null, null, null, environment);
        return new Step(dataEvaluator, fakeStepDefinition, stepExecutor, emptyList());
    }

    private static class FakeStepExecutor implements StepExecutor {

        ActionExecutionResult executionResult;

        public FakeStepExecutor(ActionExecutionResult fakeResult) {
            this.executionResult = fakeResult;
        }

        @Override
        public void execute(ScenarioExecution scenarioExecution, Target target, Step step) {
            step.updateContextFrom(executionResult);
        }
    }
}
