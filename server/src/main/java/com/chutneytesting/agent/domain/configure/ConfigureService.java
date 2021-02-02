package com.chutneytesting.agent.domain.configure;

import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.agent.domain.explore.ExploreAgentsService;
import com.chutneytesting.agent.domain.explore.ExploreResult;
import com.chutneytesting.agent.domain.network.AgentGraph;
import com.chutneytesting.agent.domain.network.ImmutableNetworkDescription;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.environment.domain.Environment;
import com.chutneytesting.environment.domain.EnvironmentRepository;
import java.util.HashSet;

public class ConfigureService {

    private final ExploreAgentsService exploreAgentsService;
    private final CurrentNetworkDescription currentNetworkDescription;
    private final LocalServerIdentifier localServerIdentifier;
    private final EnvironmentRepository environmentRepository;

    public ConfigureService(ExploreAgentsService exploreAgentsService,
                            CurrentNetworkDescription currentNetworkDescription,
                            LocalServerIdentifier localServerIdentifier,
                            EnvironmentRepository environmentRepository) {
        this.exploreAgentsService = exploreAgentsService;
        this.currentNetworkDescription = currentNetworkDescription;
        this.localServerIdentifier = localServerIdentifier;
        this.environmentRepository = environmentRepository;
    }

    public NetworkDescription configure(NetworkConfiguration networkConfiguration) {
        networkConfiguration = localServerIdentifier.withLocalHost(networkConfiguration);
        ExploreResult exploreResult = exploreAgentsService.explore(networkConfiguration);
        NetworkDescription networkDescription = buildNetworkDescription(networkConfiguration, exploreResult);
        wrapUpConfiguration(networkDescription);
        return networkDescription;
    }

    private NetworkDescription buildNetworkDescription(NetworkConfiguration networkConfiguration, ExploreResult exploreResult) {
        return ImmutableNetworkDescription.builder()
            .configuration(networkConfiguration)
            .agentGraph(AgentGraph.of(exploreResult, networkConfiguration))
            .build();
    }

    public void wrapUpConfiguration(NetworkDescription networkDescription) {
        currentNetworkDescription.switchTo(networkDescription);
        updateEnvironment(networkDescription.configuration().environmentConfiguration());
        exploreAgentsService.wrapUp(networkDescription);
    }

    private void updateEnvironment(NetworkConfiguration.EnvironmentConfiguration environmentConfigurations) {
        environmentConfigurations.stream().forEach(env -> {
            Environment environment = Environment.builder()
                .withName(env.name)
                .withDescription(env.description)
                .withTargets(new HashSet<>(env.targets))
                .build();
            environmentRepository.save(environment);
        });
    }
}
