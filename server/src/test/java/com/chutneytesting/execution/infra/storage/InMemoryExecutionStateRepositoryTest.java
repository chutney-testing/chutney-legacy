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

package com.chutneytesting.execution.infra.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.server.core.domain.execution.state.ExecutionStateRepository;
import com.chutneytesting.server.core.domain.execution.state.RunningScenarioState;
import org.junit.jupiter.api.Test;

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
