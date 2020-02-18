package com.chutneytesting.agent.api;

import static com.chutneytesting.agent.api.NodeNetworkController.CONFIGURE_URL;
import static com.chutneytesting.agent.api.NodeNetworkController.DESCRIPTION_URL;
import static com.chutneytesting.agent.api.NodeNetworkController.WRAP_UP_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.chutneytesting.WebConfiguration;
import com.chutneytesting.agent.api.dto.ExploreResultApiDto;
import com.chutneytesting.agent.api.dto.ExploreResultApiDto.AgentLinkEntity;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto;
import com.chutneytesting.agent.api.dto.NetworkDescriptionApiDto;
import com.chutneytesting.agent.api.mapper.ExploreResultApiMapper;
import com.chutneytesting.agent.api.mapper.NetworkConfigurationApiMapper;
import com.chutneytesting.agent.api.mapper.NetworkDescriptionApiMapper;
import com.chutneytesting.agent.domain.configure.ConfigureService;
import com.chutneytesting.agent.domain.configure.GetCurrentNetworkDescriptionService;
import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.agent.domain.explore.ExploreAgentsService;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.design.domain.environment.EnvironmentRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class NodeNetworkControllerTest {

    @Rule public MethodRule mockitoRule = MockitoJUnit.rule();

    @Mock
    ConfigureService configureService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) ExploreAgentsService exploreAgentsService;
    @Mock NetworkDescriptionApiMapper networkDescriptionApiMapper;
    @Mock ExploreResultApiMapper exploreResultApiMapper;
    @Mock NetworkConfigurationApiMapper networkConfigurationApiMapper;
    @Mock CurrentNetworkDescription currentNetworkDescription;
    @Mock GetCurrentNetworkDescriptionService getCurrentNetworkDescription;
    @Mock EnvironmentRepository environmentRepository;

    @InjectMocks
    NodeNetworkController controller;

    ObjectMapper objectMapper = new WebConfiguration().objectMapper();

    MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
    }

    @Test
    public void configuration_propagation_respond_with_the_graph_of_discovered_agents() throws Exception {
        NetworkConfigurationApiDto requestBody = new NetworkConfigurationApiDto();
        requestBody.creationDate = Instant.now();
        requestBody.agentNetworkConfiguration = new LinkedHashSet<>(Collections.singletonList(agentInfoDto("A", "host1", 1)));

        ExploreResultApiDto responseBody = new ExploreResultApiDto();
        responseBody.agentLinks.addAll(Arrays.asList(
            new AgentLinkEntity("A", "B"),
            new AgentLinkEntity("B", "A")));

        when(exploreResultApiMapper.from(any())).thenReturn(responseBody);

        MockHttpServletResponse response = mockMvc
            .perform(
                MockMvcRequestBuilders.post(NodeNetworkController.EXPLORE_URL)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().is2xxSuccessful()).andReturn().getResponse();

        ExploreResultApiDto exploreResultDto = objectMapper.readValue(response.getContentAsString(), ExploreResultApiDto.class);

        assertThat(exploreResultDto.agentLinks).hasSize(2);
    }

    @Test
    public void configure_endpoint_should_delegate_to_service() throws Exception {
        NetworkConfigurationApiDto dto = new NetworkConfigurationApiDto();
        dto.creationDate = Instant.now();
        dto.agentNetworkConfiguration = new HashSet<>();
        dto.environmentsConfiguration = new HashSet<>();

        NetworkDescription networkDescription = mock(NetworkDescription.class);

        when(configureService.configure(any())).thenReturn(networkDescription);
        NetworkDescriptionApiDto controllerInnerDto = new NetworkDescriptionApiDto();
        when(networkDescriptionApiMapper.toDto(networkDescription)).thenReturn(controllerInnerDto);

        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post(CONFIGURE_URL)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().is2xxSuccessful())
            .andReturn().getResponse();

        verify(configureService).configure(any());

        NetworkDescriptionApiDto result = objectMapper.readValue(response.getContentAsString(), NetworkDescriptionApiDto.class);
        assertThat(result).isNotNull();
    }

    @Test
    public void getGraph_returns_the_graph() throws Exception {

        NetworkDescription networkDescription = mock(NetworkDescription.class);
        NetworkDescriptionApiDto dto = new NetworkDescriptionApiDto();

        when(getCurrentNetworkDescription.getCurrentNetworkDescription()).thenReturn(networkDescription);
        when(networkDescriptionApiMapper.toDto(same(networkDescription))).thenReturn(dto);

        MockHttpServletResponse response = mockMvc.perform(get(DESCRIPTION_URL))
            .andExpect(status().is2xxSuccessful())
            .andReturn().getResponse();

        assertThat(response.getContentAsString()).isEqualTo(objectMapper.writeValueAsString(dto));
    }

    @Test
    public void should_propagate_network_description() throws Exception {
        NetworkDescriptionApiDto dto = new NetworkDescriptionApiDto();

        mockMvc.perform(MockMvcRequestBuilders.post(WRAP_UP_URL)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().is2xxSuccessful());

        verify(configureService).wrapUpConfiguration(any());
    }

    private NetworkConfigurationApiDto.AgentInfoApiDto agentInfoDto(String name, String host, int port) {
        NetworkConfigurationApiDto.AgentInfoApiDto agentInfoApiDto = new NetworkConfigurationApiDto.AgentInfoApiDto();
        agentInfoApiDto.name = name;
        agentInfoApiDto.host = host;
        agentInfoApiDto.port = port;
        return agentInfoApiDto;
    }
}
