package com.chutneytesting.engine.domain.execution.strategies;

import static com.chutneytesting.engine.domain.execution.ScenarioExecution.createScenarioExecution;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.report.Status;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.util.ReflectionUtils;

public class RetryWithTimeOutStrategyTest {

    private final RetryWithTimeOutStrategy strategyUnderTest = new RetryWithTimeOutStrategy();

    private StrategyProperties properties(String timeOut, String retryDelay) {
        StrategyProperties strategyProperties = new StrategyProperties();
        strategyProperties.setProperty("timeOut", timeOut);
        strategyProperties.setProperty("retryDelay", retryDelay);

        return strategyProperties;
    }

    // TODO remove this test as dup of DurationTest ?
    @Test
    public void fails_because_of_negative_parameters_durations() {
        StrategyProperties strategyProperties = properties("-1", "50 ms");
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("", strategyProperties);
        Step step = mock(Step.class);
        when(step.strategy()).thenReturn(Optional.of(strategyDefinition));
        assertThatThrownBy(() -> strategyUnderTest.execute(createScenarioExecution(null), step, null, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void fails_because_missing_parameters_durations() {
        StrategyProperties strategyProperties = properties(null, "50 ms");
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("", strategyProperties);

        Step step = mock(Step.class);
        when(step.strategy()).thenReturn(Optional.of(strategyDefinition));
        assertThatThrownBy(() -> strategyUnderTest.execute(createScenarioExecution(null), step, null, null))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void step_succeeds_execute_only_once() {
        // Given
        StrategyProperties strategyProperties = properties("100 sec", "50 ms");
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("", strategyProperties);

        // When
        Step step = mock(Step.class);
        when(step.strategy()).thenReturn(Optional.of(strategyDefinition));
        strategyUnderTest.execute(createScenarioExecution(null), step, null, null);

        // Then
        verify(step, times(1)).execute(any(), any());
    }

    @Test
    public void step_fails_retry_until_timeout_exceed() {
        StrategyProperties strategyProperties = properties("1 sec", "50 ms");
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("", strategyProperties);

        Step step = mockStep(Status.FAILURE);
        when(step.strategy()).thenReturn(Optional.of(strategyDefinition));
        long start = System.currentTimeMillis();
        ScenarioExecution scenarioExecution = createScenarioExecution(null);
        strategyUnderTest.execute(scenarioExecution, step, null, null);

        long executionDuration = System.currentTimeMillis() - start;
        assertThat(executionDuration).isBetween(1000L, 2000L);
        verify(step, atMost(20)).execute(any(), any());
        verify(step, atMost(19)).resetExecution();
    }

    @Test
    public void step_fails_do_not_retry_if_stop_requested() {
        StrategyProperties strategyProperties = properties("0.1 sec", "5 ms");
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("", strategyProperties);

        Step step = mockStep(Status.FAILURE);
        when(step.strategy()).thenReturn(Optional.of(strategyDefinition));
        ScenarioExecution scenarioExecution = createScenarioExecution(null);
        stopExecution(scenarioExecution);
        Status stepExecutedStatus = strategyUnderTest.execute(scenarioExecution, step, null, null);

        verify(step, times(1)).execute(any(), any());
        assertThat(stepExecutedStatus).isEqualTo(Status.STOPPED);
    }

    @Test
    public void step_fails_retry_until_success_execute_4_times() {
        StrategyProperties strategyProperties = properties("1 sec", "5 ms");
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("", strategyProperties);

        Step step = mockStep(Status.FAILURE, Status.FAILURE, Status.FAILURE, Status.SUCCESS);
        when(step.strategy()).thenReturn(Optional.of(strategyDefinition));
        strategyUnderTest.execute(createScenarioExecution(null), step, null, null);

        verify(step, times(4)).execute(any(), any());
    }

    @Test
    public void should_execute_all_tasks_when_status_is_not_KO() {
        Step rootStep = mock(Step.class);

        StepExecutionStrategies strategies = mock(StepExecutionStrategies.class);

        List<StepExecutionStrategy> strategiesMock = Lists.newArrayList();

        List<Step> mockSteps = Arrays.stream(Status.values())
            .filter(s -> !Status.FAILURE.equals(s))
            .map(status -> {
                Step step = mock(Step.class);
                StepStrategyDefinition sd = mock(StepStrategyDefinition.class);
                when(step.strategy()).thenReturn(Optional.of(sd));
                StepExecutionStrategy strategy = mockStrategy(status);
                when(strategies.buildStrategyFrom(step)).thenReturn(strategy);
                strategiesMock.add(strategy);
                return step;
            })
            .collect(Collectors.toList());

        when(rootStep.subSteps()).thenReturn(mockSteps);
        when(rootStep.isParentStep()).thenReturn(true);

        RetryWithTimeOutStrategy strategy = new RetryWithTimeOutStrategy();

        StrategyProperties properties = properties("2 s", "5 ms");
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("", properties);
        when(rootStep.strategy()).thenReturn(Optional.of(strategyDefinition));

        strategy.execute(createScenarioExecution(null), rootStep, null, strategies);

        strategiesMock.forEach(strat -> verify(strat, times(1)).execute(any(), any(), any(), any()));
    }

    @Test
    public void steps_container_retry_until_all_steps_in_success() {

        Step rootStep = mock(Step.class);

        Step step1 = mock(Step.class);
        StepStrategyDefinition sd1 = mock(StepStrategyDefinition.class);
        when(step1.strategy()).thenReturn(Optional.of(sd1));
        Step step2 = mock(Step.class);
        StepStrategyDefinition sd2 = mock(StepStrategyDefinition.class);
        when(step2.strategy()).thenReturn(Optional.of(sd2));
        Step step3 = mock(Step.class);
        StepStrategyDefinition sd3 = mock(StepStrategyDefinition.class);
        when(step3.strategy()).thenReturn(Optional.of(sd3));

        StepExecutionStrategy strategy1 = mockStrategy(Status.FAILURE, Status.FAILURE, Status.FAILURE, Status.SUCCESS);
        StepExecutionStrategy strategy2 = mockStrategy(Status.SUCCESS);
        StepExecutionStrategy strategy3 = mockStrategy(Status.FAILURE, Status.FAILURE, Status.FAILURE, Status.FAILURE, Status.FAILURE, Status.FAILURE, Status.SUCCESS);

        when(rootStep.subSteps()).thenReturn(Lists.newArrayList(step1, step2, step3));
        when(rootStep.isParentStep()).thenReturn(true);

        StepExecutionStrategies strategies = mock(StepExecutionStrategies.class);
        when(strategies.buildStrategyFrom(step1)).thenReturn(strategy1);
        when(strategies.buildStrategyFrom(step2)).thenReturn(strategy2);
        when(strategies.buildStrategyFrom(step3)).thenReturn(strategy3);

        StrategyProperties properties = properties("1 min", "5 ms");
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("", properties);
        when(rootStep.strategy()).thenReturn(Optional.of(strategyDefinition));

        strategyUnderTest.execute(createScenarioExecution(null), rootStep, null, strategies);

        verify(strategy1, times(10)).execute(any(), eq(step1), any(), eq(strategies));
        verify(strategy2, times(7)).execute(any(), eq(step2), any(), eq(strategies));
        verify(strategy3, times(7)).execute(any(), eq(step3), any(), eq(strategies));
    }

    @ParameterizedTest
    @MethodSource("retryStepParameters")
    public void should_reset_step_execution_before_each_retry(Status[] stepStatus) {
        StrategyProperties strategyProperties = properties("1 sec", "5 ms");
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("", strategyProperties);
        Step step = mockStep(stepStatus);
        when(step.strategy()).thenReturn(Optional.of(strategyDefinition));

        strategyUnderTest.execute(createScenarioExecution(null), step, null, null);

        verify(step, times(stepStatus.length - 1)).resetExecution();
    }

    @ParameterizedTest
    @MethodSource("retryStepParameters")
    public void should_add_information_about_strategy_and_retries(Status[] stepStatus) {
        StrategyProperties strategyProperties = properties("1 sec", "5 ms");
        StepStrategyDefinition strategyDefinition = new StepStrategyDefinition("", strategyProperties);
        Step step = mockStep(stepStatus);
        when(step.strategy()).thenReturn(Optional.of(strategyDefinition));

        strategyUnderTest.execute(createScenarioExecution(null), step, null, null);

        verify(step, times(2 * stepStatus.length))
            .addInformation(
                or(
                    and(
                        contains(strategyProperties.getProperty("timeOut", String.class)),
                        contains(strategyProperties.getProperty("retryDelay", String.class))
                    ),
                    contains("Try number")
                ));
    }

    private StepExecutionStrategy mockStrategy(Status... expectedStatus) {
        StepExecutionStrategy strategyMock = mock(StepExecutionStrategy.class);
        OngoingStubbing<Status> stub = when(strategyMock.execute(any(), any(), any(), any()));

        for (Status st : expectedStatus) {
            stub = stub.thenReturn(st);
        }

        return strategyMock;
    }

    private Step mockStep(Status... expectedStatus) {
        Step stepMock = mock(Step.class);
        OngoingStubbing<Status> stub = when(stepMock.execute(any(), any()));

        for (Status st : expectedStatus) {
            stub = stub.thenReturn(st);
        }

        return stepMock;
    }

    @SuppressWarnings("unused")
    private static Object[] retryStepParameters() {
        return new Object[] {
            new Object[]{new Status[]{Status.SUCCESS}},
            new Object[]{new Status[]{Status.FAILURE, Status.SUCCESS}},
            new Object[]{new Status[]{Status.FAILURE, Status.FAILURE, Status.SUCCESS}},
            new Object[]{new Status[]{Status.FAILURE, Status.FAILURE, Status.FAILURE, Status.SUCCESS}},
        };
    }


    private void stopExecution(ScenarioExecution scenarioExecution) {
        Field stopField = ReflectionUtils.findField(ScenarioExecution.class, "stop");
        stopField.setAccessible(true);
        ReflectionUtils.setField(stopField, scenarioExecution, true);
    }

}
