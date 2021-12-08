package com.chutneytesting.jira.api;

import com.chutneytesting.jira.domain.JiraRepository;
import com.chutneytesting.jira.domain.JiraTargetConfiguration;
import com.chutneytesting.jira.domain.JiraXrayService;
import com.chutneytesting.jira.infra.xraymodelapi.XrayTestExecTest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(JiraModuleController.BASE_URL)
@CrossOrigin(origins = "*")
public class JiraModuleController {

    public static final String BASE_URL = "/api/ui/jira/v1/";
    public static final String BASE_SCENARIO_URL = "scenario";
    public static final String BASE_CAMPAIGN_URL = "campaign";
    public static final String BASE_TEST_EXEC_URL = "testexec";
    public static final String BASE_CONFIGURATION_URL = "configuration";

    private final JiraRepository jiraRepository;
    private final JiraXrayService jiraXrayService;

    public JiraModuleController(JiraRepository jiraRepository, JiraXrayService jiraXrayService) {
        this.jiraRepository = jiraRepository;
        this.jiraXrayService = jiraXrayService;
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ') or hasAuthority('CAMPAIGN_WRITE')")
    @GetMapping(path = BASE_SCENARIO_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> getLinkedScenarios() {
        return jiraRepository.getAllLinkedScenarios();
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_READ')")
    @GetMapping(path = BASE_CAMPAIGN_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> getLinkedCampaigns() {
        return jiraRepository.getAllLinkedCampaigns();
    }

    @PreAuthorize("hasAuthority('SCENARIO_WRITE')")
    @GetMapping(path = BASE_SCENARIO_URL + "/{scenarioId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public JiraDto getByScenarioId(@PathVariable String scenarioId) {
        String jiraId = jiraRepository.getByScenarioId(scenarioId);
        return ImmutableJiraDto.builder()
            .id(jiraId)
            .chutneyId(scenarioId)
            .build();
    }

    @PreAuthorize("hasAuthority('SCENARIO_WRITE')")
    @PostMapping(path = BASE_SCENARIO_URL,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public JiraDto saveForScenario(@RequestBody JiraDto jiraDto) {
        jiraRepository.saveForScenario(jiraDto.chutneyId(), jiraDto.id());
        return ImmutableJiraDto.builder()
            .id(jiraDto.id())
            .chutneyId(jiraDto.chutneyId())
            .build();
    }

    @PreAuthorize("hasAuthority('SCENARIO_WRITE')")
    @DeleteMapping(path = BASE_SCENARIO_URL + "/{scenarioId}")
    public void removeForScenario(@PathVariable String scenarioId) {
        jiraRepository.removeForScenario(scenarioId);
    }


    @PreAuthorize("hasAuthority('CAMPAIGN_READ')")
    @GetMapping(path = BASE_CAMPAIGN_URL + "/{campaignId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public JiraDto getByCampaignId(@PathVariable String campaignId) {
        String jiraId = jiraRepository.getByCampaignId(campaignId);
        return ImmutableJiraDto.builder()
            .id(jiraId)
            .chutneyId(campaignId)
            .build();
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_WRITE')")
    @GetMapping(path = BASE_TEST_EXEC_URL + "/{testExecId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<JiraDto> getScenariosByCampaignId(@PathVariable String testExecId) {
        if (testExecId.isEmpty())
            return new ArrayList<>();

        Map<String, String> allLinkedScenarios = jiraRepository.getAllLinkedScenarios();
        Map<String, XrayTestExecTest> collect = jiraXrayService.getTestExecutionScenarios(testExecId).stream().collect(Collectors.toMap(XrayTestExecTest::getKey, Function.identity()));

        return allLinkedScenarios.entrySet()
            .stream()
            .filter(entry -> collect.get(entry.getValue()) != null)
            .map(m -> ImmutableJiraDto.builder()
                .id(m.getValue())
                .chutneyId(m.getKey())
                .lastExecStatus(collect.get(m.getValue()).getStatus())
                .build())
            .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_WRITE')")
    @PostMapping(path = BASE_CAMPAIGN_URL,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public JiraDto saveForCampaign(@RequestBody JiraDto jiraDto) {
        jiraRepository.saveForCampaign(jiraDto.chutneyId(), jiraDto.id());
        return ImmutableJiraDto.builder()
            .id(jiraDto.id())
            .chutneyId(jiraDto.chutneyId())
            .build();
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_WRITE')")
    @DeleteMapping(path = BASE_CAMPAIGN_URL + "/{campaignId}")
    public void removeForCampaign(@PathVariable String campaignId) {
        jiraRepository.removeForCampaign(campaignId);
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @GetMapping(path = BASE_CONFIGURATION_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public JiraConfigurationDto getConfiguration() {
        JiraTargetConfiguration jiraTargetConfiguration = jiraRepository.loadServerConfiguration();
        return ImmutableJiraConfigurationDto.builder()
            .url(jiraTargetConfiguration.url)
            .username(jiraTargetConfiguration.username)
            .password(jiraTargetConfiguration.password)
            .build();
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ') or hasAuthority('CAMPAIGN_READ')")
    @GetMapping(path = BASE_CONFIGURATION_URL + "/url", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getConfigurationUrl() {
        JiraTargetConfiguration jiraTargetConfiguration = jiraRepository.loadServerConfiguration();
        return jiraTargetConfiguration.url;
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping(path = BASE_CONFIGURATION_URL,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveConfiguration(@RequestBody JiraConfigurationDto jiraConfigurationDto) {
        jiraRepository.saveServerConfiguration(new JiraTargetConfiguration(jiraConfigurationDto.url(), jiraConfigurationDto.username(), jiraConfigurationDto.password()));
    }

}
