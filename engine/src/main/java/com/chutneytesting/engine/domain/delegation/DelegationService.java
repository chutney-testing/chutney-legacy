package com.chutneytesting.engine.domain.delegation;

import com.chutneytesting.engine.domain.environment.Target;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
import java.util.List;
import java.util.Optional;

public class DelegationService {

    private final StepExecutor localStepExecutor;
    private final DelegationClient delegationClient;

    public DelegationService(StepExecutor localStepExecutor,
                             DelegationClient delegationClient) {
        this.localStepExecutor = localStepExecutor;
        this.delegationClient = delegationClient;
    }

    public StepExecutor findExecutor(Optional<Target> target) {
        if (!target.isPresent() || target.get().name().isEmpty()) {
            return localStepExecutor;
        }

        List<NamedHostAndPort> agents = target.get().agents;
        if (!agents.isEmpty()) {
            NamedHostAndPort nextAgent = agents.get(0);
            // TODO should we do that here ?
            agents.remove(0);
            return new RemoteStepExecutor(delegationClient, nextAgent);
        } else {
            return localStepExecutor;
        }
    }
}
