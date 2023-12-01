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

import com.chutneytesting.server.core.domain.execution.state.ExecutionStateRepository;
import com.chutneytesting.server.core.domain.execution.state.RunningScenarioState;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryExecutionStateRepository implements ExecutionStateRepository {

    private final Map<String, RunningScenarioState> internalStorage = new ConcurrentHashMap<>();

    @Override
    public Set<RunningScenarioState> runningScenarios() {
        return new HashSet<>(internalStorage.values());
    }

    @Override
    public void notifyExecutionStart(final String scenarioId) {
        internalStorage.put(scenarioId, RunningScenarioState.of(scenarioId));
    }

    @Override
    public void notifyExecutionEnd(String scenarioId) {
        internalStorage.remove(scenarioId);
    }

    @Override
    public Optional<RunningScenarioState> runningState(String scenarioId) {
        return Optional.ofNullable(internalStorage.get(scenarioId));
    }
}
