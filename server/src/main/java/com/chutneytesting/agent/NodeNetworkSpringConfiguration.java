package com.chutneytesting.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.chutneytesting.agent.domain.configure.ConfigureService;
import com.chutneytesting.agent.domain.configure.Explorations;
import com.chutneytesting.agent.domain.configure.GetCurrentNetworkDescriptionService;
import com.chutneytesting.agent.domain.configure.LocalServerIdentifier;
import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.agent.domain.explore.ExploreAgentsService;
import com.chutneytesting.design.domain.environment.EnvironmentRepository;
import com.chutneytesting.agent.domain.AgentClient;
import com.chutneytesting.engine.domain.delegation.ConnectionChecker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NodeNetworkSpringConfiguration {

    private static final String NODE_NETWORK_QUALIFIER ="agentnetwork";

    @Bean
    @Qualifier(NODE_NETWORK_QUALIFIER)
    public RestTemplate restTemplateForHttpNodeNetwork(ObjectMapper objectMapper) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Lists.newArrayList(new MappingJackson2HttpMessageConverter(objectMapper)));
        return restTemplate;
    }

    @Bean
    ExploreAgentsService agentNetwork(Explorations explorations,
                                      CurrentNetworkDescription currentNetworkDescription,
                                      AgentClient agentClient,
                                      ConnectionChecker connectionChecker,
                                      LocalServerIdentifier localServerIdentifier) {
        return new ExploreAgentsService(
            explorations,
            agentClient,
            connectionChecker,
            localServerIdentifier);
    }


    @Bean
    ConfigureService configureService(ExploreAgentsService exploreAgentsService,
                                      CurrentNetworkDescription currentNetworkDescription,
                                      LocalServerIdentifier localServerIdentifier,
                                      EnvironmentRepository environmentRepository){
        return new ConfigureService(exploreAgentsService, currentNetworkDescription, localServerIdentifier, environmentRepository);
    }

    @Bean
    GetCurrentNetworkDescriptionService getCurrentNetworkDescriptionService(CurrentNetworkDescription currentNetworkDescription) {
        return new GetCurrentNetworkDescriptionService(currentNetworkDescription);
    }
}
