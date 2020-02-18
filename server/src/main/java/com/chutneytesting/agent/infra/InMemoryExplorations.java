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
