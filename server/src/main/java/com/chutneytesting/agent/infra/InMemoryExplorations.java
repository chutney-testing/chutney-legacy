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

package com.chutneytesting.agent.infra;

import com.chutneytesting.agent.domain.configure.ConfigurationState;
import com.chutneytesting.agent.domain.configure.Explorations;
import com.chutneytesting.agent.domain.configure.NetworkConfiguration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryExplorations implements Explorations {

    private final Map<NetworkConfiguration, ConfigurationState> networkConfigurationsState = new ConcurrentHashMap<>();

    // concurrent race problems don't need to be resolved, they can result several exploration of same agent...
    // which is not a problem
    @Override public boolean changeStateToIfPossible(NetworkConfiguration networkConfiguration, ConfigurationState newState) {
        ConfigurationState currentState = networkConfigurationsState.getOrDefault(networkConfiguration, ConfigurationState.NOT_STARTED);
        if (currentState.canChangeTo(newState)) {
            networkConfigurationsState.put(networkConfiguration, newState);
            return true;
        }
        return false;
    }

    @Override public void remove(NetworkConfiguration networkConfiguration) {
        networkConfigurationsState.remove(networkConfiguration);
    }
}
