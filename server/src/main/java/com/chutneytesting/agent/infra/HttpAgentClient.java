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

import static com.chutneytesting.agent.api.NodeNetworkController.EXPLORE_URL;
import static com.chutneytesting.agent.api.NodeNetworkController.WRAP_UP_URL;

import com.chutneytesting.agent.api.dto.ExploreResultApiDto;
import com.chutneytesting.agent.api.dto.ExploreResultApiDto.AgentLinkEntity;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto;
import com.chutneytesting.agent.api.dto.NetworkDescriptionApiDto;
import com.chutneytesting.agent.api.mapper.AgentGraphApiMapper;
import com.chutneytesting.agent.api.mapper.AgentInfoApiMapper;
import com.chutneytesting.agent.api.mapper.EnvironmentApiMapper;
import com.chutneytesting.agent.api.mapper.ExploreResultApiMapper;
import com.chutneytesting.agent.api.mapper.NetworkConfigurationApiMapper;
import com.chutneytesting.agent.api.mapper.NetworkDescriptionApiMapper;
import com.chutneytesting.agent.domain.AgentClient;
import com.chutneytesting.agent.domain.configure.NetworkConfiguration;
import com.chutneytesting.agent.domain.explore.ExploreResult;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.engine.domain.delegation.ConnectionChecker;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
class HttpAgentClient implements AgentClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpAgentClient.class);

    private final RestTemplate restTemplate;
    private final ConnectionChecker connectionChecker;

    private final NetworkConfigurationApiMapper networkConfigurationApiMapper = new NetworkConfigurationApiMapper(new AgentInfoApiMapper(), new EnvironmentApiMapper());
    private final NetworkDescriptionApiMapper networkDescriptionApiMapper = new NetworkDescriptionApiMapper(
        new NetworkConfigurationApiMapper(new AgentInfoApiMapper(), new EnvironmentApiMapper()),
        new AgentGraphApiMapper(new AgentInfoApiMapper()));
    private final ExploreResultApiMapper exploreResultApiMapper = new ExploreResultApiMapper();

    /**
     * @param connectionChecker used to rapidly fail if the connection to an agent can't be established.<br>
     *                          This is useful as {@link #explore} may take a while before returning.<br>
     *                          So timeout of the supplied {@link RestTemplate} used internally must be kept to a high value whereas the given {@link ConnectionChecker} must have a minimal timeout.
     */
    HttpAgentClient(RestTemplate restTemplate, ConnectionChecker connectionChecker) throws UnknownHostException {
        this.restTemplate = restTemplate;
        this.connectionChecker = connectionChecker;
    }

    /**
     * May take a while before returning, and so timeout here must be kept to a high value.
     *
     * @see AgentClient#explore
     */
    @Override
    public ExploreResult explore(String localName, NamedHostAndPort agentInfo, NetworkConfiguration networkConfiguration) {
        if (!connectionChecker.canConnectTo(agentInfo)) return ExploreResult.EMPTY;
        return exploreByHttp(localName, agentInfo, networkConfiguration);
    }

    @Override
    public void wrapUp(NamedHostAndPort agentInfo, NetworkDescription networkDescription) {
        if (connectionChecker.canConnectTo(agentInfo)) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<NetworkDescriptionApiDto> request = new HttpEntity<>(networkDescriptionApiMapper.toDto(networkDescription), headers);
            restTemplate.postForObject("https://" + agentInfo.host() + ":" + agentInfo.port() + WRAP_UP_URL, request, Void.class);
        }
    }

    private ExploreResult exploreByHttp(String localName, NamedHostAndPort agentInfo, NetworkConfiguration networkConfiguration) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<NetworkConfigurationApiDto> request = new HttpEntity<>(networkConfigurationApiMapper.toDto(networkConfiguration), headers);
            ExploreResultApiDto response = restTemplate.postForObject("https://" + agentInfo.host() + ":" + agentInfo.port() + EXPLORE_URL, request, ExploreResultApiDto.class);
            return exploreResultApiMapper.fromDto(response, new AgentLinkEntity(localName, agentInfo.name()));
        } catch (RestClientException e) {
            LOGGER.warn("Unable to propagate configure to reachable agent : " + agentInfo + " (" + e.getMessage() + ")");
            return ExploreResult.EMPTY;
        }
    }
}
