package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.CONF;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.design.domain.plugins.jira.JiraRepository;
import com.chutneytesting.design.domain.plugins.jira.JiraTargetConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ChutneyJiraPluginContent implements ChutneyContentProvider {

    private final JiraRepository repository;
    private final ObjectMapper mapper;

    public ChutneyJiraPluginContent(JiraRepository repository, @Qualifier("gitObjectMapper") ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public String provider() {
        return "jira_plugin";
    }

    @Override
    public ChutneyContentCategory category() {
        return CONF;
    }

    @Override
    public Stream<ChutneyContent> getContent() {
        try {
            return Stream.of(
                build("jira_config", mapper.writeValueAsString(repository.loadServerConfiguration())),
                build("scenario_link", mapper.writeValueAsString(repository.getAllLinkedScenarios())),
                build("campaign_link", mapper.writeValueAsString(repository.getAllLinkedCampaigns()))
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    private ChutneyContent build(String name, String content) {
        return ChutneyContent.builder()
            .withProvider(provider())
            .withCategory(category())
            .withName(name)
            .withFormat("json")
            .withContent(content)
            .build();
    }

    @Override
    public void importDefaultFolder(Path workingDirectory) {
        importFolder(workingDirectory);
    }

    public void importFolder(Path folderPath) {
        importJiraConfiguration(providerFolder(folderPath).resolve("jira_config.json"));
        importScenarioLinks(providerFolder(folderPath).resolve("scenario_link.json"));
        importCampaignLinks(providerFolder(folderPath).resolve("campaign_link.json"));
    }

    private void importJiraConfiguration(Path filePath) {
        if (Files.exists(filePath)) {
            try {
                byte[] bytes = Files.readAllBytes(filePath);
                try {
                    config(bytes);
                } catch (IOException e) {
                    throw new UnsupportedOperationException("Cannot deserialize file", e);
                }
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot read file", e);
            }
        }
    }

    private void importScenarioLinks(Path filePath) {
        if (Files.exists(filePath)) {
            try {
                byte[] bytes = Files.readAllBytes(filePath);
                try {
                    scenarios(bytes);
                } catch (IOException e) {
                    throw new UnsupportedOperationException("Cannot deserialize file", e);
                }
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot read file", e);
            }
        }
    }

    private void importCampaignLinks(Path filePath) {
        if (Files.exists(filePath)) {
            try {
                byte[] bytes = Files.readAllBytes(filePath);
                try {
                    campaigns(bytes);
                } catch (IOException e) {
                    throw new UnsupportedOperationException("Cannot deserialize file", e);
                }
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot read file", e);
            }
        }
    }

    private void config(byte[] bytes) throws IOException {
        JiraTargetConfiguration targetConfiguration = mapper.readValue(bytes, JiraTargetConfiguration.class);
        repository.saveServerConfiguration(targetConfiguration);
    }

    private void scenarios(byte[] bytes) throws IOException {
        Map<String, String> scenarios = mapper.readValue(bytes, Map.class);
        scenarios.keySet().forEach(k -> repository.saveForScenario(k, scenarios.get(k)));
    }

    private void campaigns(byte[] bytes) throws IOException {
        Map<String, String> campaigns = mapper.readValue(bytes, Map.class);
        campaigns.keySet().forEach(k -> repository.saveForCampaign(k, campaigns.get(k)));
    }
}
