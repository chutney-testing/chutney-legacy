package com.chutneytesting.agent.infra.storage;

import com.chutneytesting.agent.domain.configure.LocalServerIdentifier;
import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.agent.domain.network.Agent;
import com.chutneytesting.agent.domain.network.ImmutableNetworkDescription;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import com.chutneytesting.environment.api.EnvironmentApi;
import java.io.OutputStream;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JsonFileCurrentNetworkDescription implements CurrentNetworkDescription {

    private final EnvironmentApi environmentApi;
    private final AgentNetworkMapperJsonFileMapper agentNetworkMapperJsonFileMapper;
    private final JsonFileAgentNetworkDao jsonFileAgentNetworkDao;
    private final LocalServerIdentifier localServerIdentifier;

    private Optional<NetworkDescription> networkDescription;

    public JsonFileCurrentNetworkDescription(
        EmbeddedEnvironmentApi environmentApi,
        AgentNetworkMapperJsonFileMapper agentNetworkMapperJsonFileMapper,
        JsonFileAgentNetworkDao jsonFileAgentNetworkDao, LocalServerIdentifier localServerIdentifier) {
        this.environmentApi = environmentApi;
        this.agentNetworkMapperJsonFileMapper = agentNetworkMapperJsonFileMapper;
        this.jsonFileAgentNetworkDao = jsonFileAgentNetworkDao;
        this.localServerIdentifier = localServerIdentifier;
        this.networkDescription = getNetworkDescription();
    }

    @Override
    public Optional<NetworkDescription> findCurrent() {
        networkDescription = getNetworkDescription();
        return networkDescription;
    }

    @Override
    public void switchTo(NetworkDescription networkDescription) {
        AgentNetworkForJsonFile dto = agentNetworkMapperJsonFileMapper.toDto(networkDescription);
        jsonFileAgentNetworkDao.save(dto);
    }

    @Override
    public void backup(OutputStream outputStream) {
        if (getNetworkDescription().isPresent()) {
            jsonFileAgentNetworkDao.backup(outputStream);
        }
    }

    @Override
    public String name() {
        return "agents";
    }

    private Optional<NetworkDescription> getNetworkDescription() {
        Optional<NetworkDescription> newNetworkDescription = jsonFileAgentNetworkDao.read()
            .map(dto -> agentNetworkMapperJsonFileMapper.fromDto(dto, environmentApi.listEnvironments()));

        if (newNetworkDescription.isPresent()) {
            final Agent localAgent = localServerIdentifier.findLocalAgent(newNetworkDescription.get().agentGraph());
            newNetworkDescription = Optional.of(ImmutableNetworkDescription.builder().from(newNetworkDescription.get()).localAgent(localAgent).build());
        }

        return newNetworkDescription;
    }
}
