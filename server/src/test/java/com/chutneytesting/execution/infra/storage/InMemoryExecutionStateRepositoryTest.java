package com.chutneytesting.execution.infra.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.execution.domain.state.ExecutionStateRepository;
import com.chutneytesting.execution.domain.state.RunningScenarioState;
import org.junit.Test;

public class InMemoryExecutionStateRepositoryTest {

    @Test
    public void store_is_empty_on_startup() {
        ExecutionStateRepository store = new InMemoryExecutionStateRepository();

        assertThat(store.runningScenarios()).hasSize(0);
    }

    @Test
    public void store_increments_during_execution_of_one_scenario() {
        ExecutionStateRepository store = new InMemoryExecutionStateRepository();

        store.notifyExecutionStart("1");
        assertThat(store.runningScenarios()).hasSize(1);
        store.notifyExecutionEnd("1");

        assertThat(store.runningScenarios()).hasSize(0);
    }

    @Test
    public void store_increments_during_execution_of_multiple_scenarios() {
        ExecutionStateRepository store = new InMemoryExecutionStateRepository();

        store.notifyExecutionStart("1");
        assertThat(store.runningScenarios()).hasSize(1);
        store.notifyExecutionStart("2");
        assertThat(store.runningScenarios()).hasSize(2);
        store.notifyExecutionStart("42");
        assertThat(store.runningScenarios()).hasSize(3);
        store.notifyExecutionEnd("2");
        assertThat(store.runningScenarios()).hasSize(2);
        store.notifyExecutionEnd("1");
        assertThat(store.runningScenarios()).hasSize(1);
        store.notifyExecutionEnd("42");
        assertThat(store.runningScenarios()).hasSize(0);
    }

    @Test
    public void lookup_for_a_not_running_scenario_returns_empty() {
        ExecutionStateRepository store = new InMemoryExecutionStateRepository();

        assertThat(store.runningState("1")).isEmpty();
    }

    @Test
    public void lookup_for_a_running_scenario_returns_its_running_state() {
        ExecutionStateRepository store = new InMemoryExecutionStateRepository();

        store.notifyExecutionStart("53");

        assertThat(store.runningState("1")).isEmpty();
        assertThat(store.runningState("53")).map(RunningScenarioState::scenarioId).contains("53");

        store.notifyExecutionEnd("53");

        assertThat(store.runningState("53")).isEmpty();
    }
}
