package com.chutneytesting.execution.infra.storage;

import com.chutneytesting.execution.domain.state.ExecutionStateRepository;
import com.chutneytesting.execution.domain.state.RunningScenarioState;
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
