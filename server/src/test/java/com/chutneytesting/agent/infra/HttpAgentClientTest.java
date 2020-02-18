package com.chutneytesting.agent.infra;

import static com.chutneytesting.agent.api.NodeNetworkController.EXPLORE_URL;
import static com.chutneytesting.agent.api.NodeNetworkController.WRAP_UP_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.chutneytesting.WebConfiguration;
import com.chutneytesting.agent.NodeNetworkSpringConfiguration;
import com.chutneytesting.agent.api.dto.ExploreResultApiDto;
import com.chutneytesting.agent.api.dto.ExploreResultApiDto.AgentLinkEntity;
import com.chutneytesting.agent.domain.configure.ImmutableNetworkConfiguration;
import com.chutneytesting.agent.domain.configure.ImmutableNetworkConfiguration.AgentNetworkConfiguration;
import com.chutneytesting.agent.domain.configure.ImmutableNetworkConfiguration.EnvironmentConfiguration;
import com.chutneytesting.agent.domain.configure.LocalServerIdentifier;
import com.chutneytesting.agent.domain.AgentClient;
import com.chutneytesting.agent.domain.explore.ExploreResult;
import com.chutneytesting.engine.domain.delegation.ConnectionChecker;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.agent.domain.configure.NetworkConfiguration;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

public class HttpAgentClientTest {

    @Rule public MethodRule mockitoRule = MockitoJUnit.rule();

    AgentClient agentClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) LocalServerIdentifier localServerIdentifier;
    @Mock ConnectionChecker connectionChecker;

    ObjectMapper objectMapper = new WebConfiguration().objectMapper();
    RestTemplate restTemplate = new NodeNetworkSpringConfiguration().restTemplateForHttpNodeNetwork(objectMapper);
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

    @Before
    public void setUp() throws UnknownHostException {
        agentClient = new HttpAgentClient(restTemplate, connectionChecker);
    }

    @Test
    public void client_returns_empty_links_if_localhost() {
        when(connectionChecker.canConnectTo(any())).thenReturn(false);

        ExploreResult exploreResult = agentClient.explore("", agentInfo("testName", "test", 0), null);

        assertThat(exploreResult.agentLinks()).hasSize(0);
    }

    @Test
    public void client_returns_empty_links_if_unreachable_because_connectionTester_fails() {
        when(connectionChecker.canConnectTo(any())).thenReturn(false);

        ExploreResult exploreResult = agentClient.explore("local", agentInfo("testName", "host", 1), null);

        assertThat(exploreResult.agentLinks()).hasSize(0);
    }

    @Test
    public void client_returns_empty_links_if_unreachable_because_restTemplate_throws() {
        when(connectionChecker.canConnectTo(any())).thenReturn(true);
        server.expect(manyTimes(), requestTo("https://host:1" + EXPLORE_URL)).andExpect(method(HttpMethod.POST))
            .andRespond(request -> {
                throw new IOException("Unreachable");
            });

        ExploreResult exploreResult = agentClient.explore("local", agentInfo("testName", "host", 1), buildNetworkConfiguration());

        assertThat(exploreResult.agentLinks()).hasSize(0);
    }

    @Test
    public void client_returns_empty_links_if_response_code_is_not_2XX() {
        when(connectionChecker.canConnectTo(any())).thenReturn(true);
        server.expect(manyTimes(), requestTo("https://host:1" + EXPLORE_URL)).andExpect(method(HttpMethod.POST))
            .andRespond(withServerError());

        ExploreResult exploreResult = agentClient.explore("local", agentInfo("testName", "host", 1), buildNetworkConfiguration());

        assertThat(exploreResult.agentLinks()).hasSize(0);
    }

    @Test
    public void client_returns_link_to_remote_and_remote_links_otherwise() throws JsonProcessingException {
        when(connectionChecker.canConnectTo(any())).thenReturn(true);

        String configurationCreationInstant = "2019-08-23T09:52:35Z";
        ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(Include.NON_EMPTY);
        ExploreResultApiDto exploreResultApiDto = new ExploreResultApiDto();

        exploreResultApiDto.agentLinks.addAll(Arrays.asList(new AgentLinkEntity("B", "A"),
            new AgentLinkEntity("B", "C")));

        String responseBody = objectMapper.writeValueAsString(exploreResultApiDto);

        NamedHostAndPort agentInfoB = agentInfo("B", "host2", 1);
        server.expect(manyTimes(), requestTo("https://" + agentInfoB.host() + ":" + agentInfoB.port() + EXPLORE_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.creationDate").value(configurationCreationInstant))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON_UTF8));

        NetworkConfiguration networkConfiguration = buildNetworkConfiguration(Instant.parse(configurationCreationInstant),
            agentInfo("A", "host1", 1),
            agentInfoB,
            agentInfo("C", "host3", 1));

        ExploreResult exploreResult = agentClient.explore("A", agentInfoB, networkConfiguration);

        assertThat(exploreResult.agentLinks()).hasSize(3).extracting(link -> link.source().name() + "->" + link.destination().name()).containsExactlyInAnyOrder("A->B", "B->A", "B->C");
    }

    @Test
    public void wrapup_should_call_remote() {
        NetworkDescription mock = mock(NetworkDescription.class);
        NamedHostAndPort agentInfo = new NamedHostAndPort("name", "host", 1000);

        server.expect(manyTimes(), requestTo("https://host:1000" + WRAP_UP_URL)).andExpect(method(HttpMethod.POST))
            .andRespond(MockRestResponseCreators.withSuccess());

        agentClient.wrapUp(agentInfo, mock);
    }

    @Test
    public void wrapup_does_nothing_if_remote_is_not_joinable() throws Exception {
        restTemplate = mock(RestTemplate.class);
        agentClient = new HttpAgentClient(restTemplate, connectionChecker);

        NetworkDescription mock = mock(NetworkDescription.class);
        NamedHostAndPort info = mock(NamedHostAndPort.class);

        when(connectionChecker.canConnectTo(any())).thenReturn(false);

        agentClient.wrapUp(info, mock);

        verifyNoMoreInteractions(restTemplate);
    }

    private NetworkConfiguration buildNetworkConfiguration(NamedHostAndPort... agentInfos) {
        return buildNetworkConfiguration(Instant.now(), agentInfos);
    }

    private NetworkConfiguration buildNetworkConfiguration(Instant creationDate, NamedHostAndPort... agentInfos) {
        return ImmutableNetworkConfiguration.builder()
            .creationDate(creationDate)
            .agentNetworkConfiguration(
                AgentNetworkConfiguration.builder().addAgentInfos(agentInfos).build()
            )
            .environmentConfiguration(EnvironmentConfiguration.builder().build())
            .build();
    }

    static NamedHostAndPort agentInfo(String name, String host, int port) {
        return new NamedHostAndPort(name, host, port);
    }
}
