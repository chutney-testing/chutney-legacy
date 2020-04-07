package com.chutneytesting.engine.domain.delegation;

import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
import com.chutneytesting.task.spi.injectable.Target;
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

        List<NamedHostAndPort> agents = ((TargetImpl) target.get()).agents;
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
