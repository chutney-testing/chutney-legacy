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

package com.chutneytesting.agent.infra.storage;

import com.chutneytesting.agent.domain.configure.LocalServerIdentifier;
import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.agent.domain.network.Agent;
import com.chutneytesting.agent.domain.network.ImmutableNetworkDescription;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.environment.api.environment.EmbeddedEnvironmentApi;
import com.chutneytesting.environment.api.environment.EnvironmentApi;
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
