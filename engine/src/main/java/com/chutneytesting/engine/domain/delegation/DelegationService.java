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

package com.chutneytesting.engine.domain.delegation;

import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
import com.chutneytesting.action.spi.injectable.Target;
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
        if (target.isEmpty() || target.get().name().isEmpty()) {
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
