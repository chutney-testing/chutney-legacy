package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.CONF;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.design.domain.plugins.jira.JiraRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        ChutneyContent.ChutneyContentBuilder configurationBuilder = ChutneyContent.builder();
        ChutneyContent.ChutneyContentBuilder scenariosBuilder = ChutneyContent.builder();
        ChutneyContent.ChutneyContentBuilder campaignsBuilder = ChutneyContent.builder();

        try {
            configurationBuilder
                .withProvider(provider())
                .withCategory(category())
                .withName("jira_config")
                .withFormat("json")
                .withContent(mapper.writeValueAsString(repository.loadServerConfiguration()))
                .build();

            scenariosBuilder
                .withProvider(provider())
                .withCategory(category())
                .withName("scenario_link")
                .withContent(mapper.writeValueAsString(repository.getAllLinkedScenarios()))
                .build();

            campaignsBuilder
                .withProvider(provider())
                .withCategory(category())
                .withName("campaign_link")
                .withContent(mapper.writeValueAsString(repository.getAllLinkedCampaigns()))
                .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return Stream.of(
            configurationBuilder.build(),
            scenariosBuilder.build(),
            campaignsBuilder.build()
        );
    }
}
