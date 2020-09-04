package com.chutneytesting.design.api.jira;

import com.chutneytesting.design.domain.jira.JiraRepository;
import com.chutneytesting.design.domain.jira.JiraTargetConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(com.chutneytesting.design.api.jira.JiraModuleController.BASE_URL)
@CrossOrigin(origins = "*")
public class JiraModuleController {

    public static final String BASE_URL = "/api/ui/jira/v1/";
    public static final String BASE_SCENARIO_URL = "scenario";
    public static final String BASE_CAMPAIGN_URL = "campaign";
    public static final String BASE_CONFIGURATION_URL = "configuration";

    private final JiraRepository jiraRepository;

    public JiraModuleController(JiraRepository jiraRepository) {
        this.jiraRepository = jiraRepository;
    }


    @GetMapping(path = BASE_SCENARIO_URL + "/{scenarioId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public JiraDto getByScenarioId(@PathVariable String scenarioId) {
        String jiraId = jiraRepository.getByScenarioId(scenarioId);
        return ImmutableJiraDto.builder()
            .id(jiraId)
            .chutneyId(scenarioId)
            .build();
    }

    @PostMapping(path = BASE_SCENARIO_URL,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public JiraDto saveForScenario(@RequestBody JiraDto jiraDto) {
        jiraRepository.saveForScenario(jiraDto.chutneyId(), jiraDto.id());
        return ImmutableJiraDto.builder()
            .id(jiraDto.id())
            .chutneyId(jiraDto.chutneyId())
            .build();
    }

    @GetMapping(path = BASE_CAMPAIGN_URL + "/{campaignId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public JiraDto getByCampaignId(@PathVariable String campaignId) {
        String jiraId = jiraRepository.getByCampaignId(campaignId);
        return ImmutableJiraDto.builder()
            .id(jiraId)
            .chutneyId(campaignId)
            .build();
    }

    @PostMapping(path = BASE_CAMPAIGN_URL,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public JiraDto saveForCampaign(@RequestBody JiraDto jiraDto) {
        jiraRepository.saveForCampaign(jiraDto.chutneyId(), jiraDto.id());
        return ImmutableJiraDto.builder()
            .id(jiraDto.id())
            .chutneyId(jiraDto.chutneyId())
            .build();
    }

    @GetMapping(path = BASE_CONFIGURATION_URL , produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public JiraConfigurationDto getConfiguration() {
        JiraTargetConfiguration jiraTargetConfiguration = jiraRepository.loadServerConfiguration();
        return ImmutableJiraConfigurationDto.builder()
            .url(jiraTargetConfiguration.url)
            .username(jiraTargetConfiguration.username)
            .password(jiraTargetConfiguration.password)
            .build();
    }

    @PostMapping(path = BASE_CONFIGURATION_URL,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void saveConfiguration(@RequestBody JiraConfigurationDto jiraConfigurationDto) {
        jiraRepository.saveServerConfiguration(new JiraTargetConfiguration(jiraConfigurationDto.url(),jiraConfigurationDto.username(),jiraConfigurationDto.password()));
    }

}
