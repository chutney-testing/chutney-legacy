package com.chutneytesting.jira.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.chutneytesting.jira.domain.JiraRepository;
import com.chutneytesting.jira.domain.JiraTargetConfiguration;
import com.chutneytesting.jira.domain.JiraXrayApi;
import com.chutneytesting.jira.domain.JiraXrayService;
import com.chutneytesting.jira.infra.JiraFileRepository;
import com.chutneytesting.jira.infra.xraymodelapi.XrayTestExecTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class JiraModuleControllerTest {

    private JiraRepository jiraRepository;
    private final JiraXrayApi jiraXrayApiMock = mock(JiraXrayApi.class);
    private MockMvc mockMvc;

    private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();


    @BeforeEach
    public void setUp() throws IOException {
        jiraRepository = new JiraFileRepository(Files.createTempDirectory("jira").toString());
        JiraXrayService jiraXrayService = new JiraXrayService(jiraRepository, jiraXrayApiMock);
        jiraRepository.saveServerConfiguration(new JiraTargetConfiguration("an url", "a username", "a password"));
        jiraRepository.saveForCampaign("10", "JIRA-10");
        jiraRepository.saveForCampaign("20", "JIRA-20");
        jiraRepository.saveForScenario("1", "SCE-1");
        jiraRepository.saveForScenario("2", "SCE-2");
        jiraRepository.saveForScenario("3", "SCE-3");

        JiraModuleController jiraModuleController = new JiraModuleController(jiraRepository, jiraXrayService);
        mockMvc = MockMvcBuilders.standaloneSetup(jiraModuleController).build();
    }

    @Test
    void getLinkedScenarios() {
        Map<String, String> map = getJiraController("/api/ui/jira/v1/scenario", new TypeReference<>() {
        });

        assertThat(map).hasSize(3);
        assertThat(map).containsOnly(entry("1", "SCE-1"), entry("2", "SCE-2"), entry("3", "SCE-3"));
    }

    @Test
    void getLinkedCampaigns() {
        Map<String, String> map = getJiraController("/api/ui/jira/v1/campaign", new TypeReference<>() {
        });

        assertThat(map).hasSize(2);
        assertThat(map).containsOnly(entry("10", "JIRA-10"), entry("20", "JIRA-20"));
    }

    @Test
    void getByScenarioId() {
        JiraDto jiraDto = getJiraController("/api/ui/jira/v1/scenario/1", new TypeReference<>() {
        });

        assertThat(jiraDto.chutneyId()).isEqualTo("1");
        assertThat(jiraDto.id()).isEqualTo("SCE-1");
    }

    @Test
    void saveForScenario() {
        JiraDto dto = ImmutableJiraDto.builder().chutneyId("123").id("SCE-123").build();
        JiraDto jiraDto = postJiraController("/api/ui/jira/v1/scenario", new TypeReference<>() {
        }, dto);

        assertThat(jiraDto.chutneyId()).isEqualTo("123");
        assertThat(jiraDto.id()).isEqualTo("SCE-123");
        assertThat(jiraRepository.getAllLinkedScenarios()).contains(entry("123", "SCE-123"));
    }

    @Test
    void removeForScenario() {
        deleteJiraController("/api/ui/jira/v1/scenario/1");

        assertThat(jiraRepository.getByScenarioId("1")).isEmpty();
    }

    @Test
    void getByCampaignId() {
        JiraDto jiraDto = getJiraController("/api/ui/jira/v1/campaign/10", new TypeReference<>() {
        });

        assertThat(jiraDto.chutneyId()).isEqualTo("10");
        assertThat(jiraDto.id()).isEqualTo("JIRA-10");
    }

    @Test
    void getScenariosByCampaignId() {

        List<XrayTestExecTest> result = new ArrayList<>();
        XrayTestExecTest xrayTestExecTest = new XrayTestExecTest();
        xrayTestExecTest.setId("12345");
        xrayTestExecTest.setKey("SCE-2");
        xrayTestExecTest.setStatus("PASS");
        result.add(xrayTestExecTest);

        when(jiraXrayApiMock.getTestExecutionScenarios(anyString(), any())).thenReturn(result);

        List<JiraDto> scenarios = getJiraController("/api/ui/jira/v1/testexec/JIRA-10", new TypeReference<>() {
        });

        assertThat(scenarios).hasSize(1);
        assertThat(scenarios.get(0).id()).isEqualTo("SCE-2");
        assertThat(scenarios.get(0).chutneyId()).isEqualTo("2");
        assertThat(scenarios.get(0).lastExecStatus().get()).isEqualTo("PASS");
    }

    @Test
    void saveForCampaign() {
        JiraDto dto = ImmutableJiraDto.builder().chutneyId("123").id("JIRA-123").build();
        JiraDto jiraDto = postJiraController("/api/ui/jira/v1/campaign", new TypeReference<>() {}, dto);

        assertThat(jiraDto.chutneyId()).isEqualTo("123");
        assertThat(jiraDto.id()).isEqualTo("JIRA-123");
        assertThat(jiraRepository.getAllLinkedCampaigns()).contains(entry("123", "JIRA-123"));
    }

    @Test
    void removeForCampaign() {
        deleteJiraController("/api/ui/jira/v1/campaign/10");

        assertThat(jiraRepository.getByCampaignId("10")).isEmpty();
    }

    @Test
    void getConfiguration() {
        JiraConfigurationDto configurationDto = getJiraController("/api/ui/jira/v1/configuration", new TypeReference<>() {
        });

        assertThat(configurationDto.url()).isEqualTo("an url");
        assertThat(configurationDto.username()).isEqualTo("a username");
        assertThat(configurationDto.password()).isEqualTo("a password");
    }

    @Test
    void getConfigurationUrl() throws Exception {
        String url = mockMvc.perform(MockMvcRequestBuilders.get("/api/ui/jira/v1/configuration/url")
            .accept(MediaType.TEXT_PLAIN_VALUE)) // <--
            .andDo(print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(url).isEqualTo("an url");
    }

    @Test
    void saveConfiguration() throws Exception {
        JiraTargetConfiguration newConfiguration = new JiraTargetConfiguration("a new url", "a new username", "a new password");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/ui/jira/v1/configuration")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(om.writeValueAsString(newConfiguration))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(MockMvcResultMatchers.status().isOk());

        JiraTargetConfiguration expected = jiraRepository.loadServerConfiguration();
        assertThat(expected).usingRecursiveComparison().isEqualTo(newConfiguration);
    }

    private <T> T getJiraController(String url, TypeReference<T> typeReference) {
        try {
            String contentAsString = mockMvc.perform(MockMvcRequestBuilders.get(url)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
            return om.readValue(contentAsString, typeReference);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteJiraController(String url) {
        try {
            mockMvc.perform(MockMvcRequestBuilders.delete(url)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T postJiraController(String url, TypeReference<T> typeReference, Object object) {
        try {
            String contentAsString = mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(om.writeValueAsString(object))
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
            return om.readValue(contentAsString, typeReference);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
