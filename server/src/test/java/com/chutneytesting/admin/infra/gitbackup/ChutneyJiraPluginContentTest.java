package com.chutneytesting.admin.infra.gitbackup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.infra.gitbackup.ChutneyJiraPluginContent;
import com.chutneytesting.design.domain.plugins.jira.JiraRepository;
import com.chutneytesting.design.domain.plugins.jira.JiraTargetConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class ChutneyJiraPluginContentTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void should_return_jira_plugin_content() {
        // Given
        JiraRepository repositoryMock = mock(JiraRepository.class);

        JiraTargetConfiguration configuration = new JiraTargetConfiguration("url", "username", "password");
        when(repositoryMock.loadServerConfiguration()).thenReturn(configuration);

        Map<String, String> scenarios = Map.of(
            "42", "4242"
        );

        when(repositoryMock.getAllLinkedScenarios()).thenReturn(scenarios);

        Map<String, String> campaigns = Map.of(
            "13", "37"
        );
        when(repositoryMock.getAllLinkedCampaigns()).thenReturn(campaigns);

        // When
        ChutneyJiraPluginContent sut = new ChutneyJiraPluginContent(repositoryMock, objectMapper);
        List<ChutneyContent> actual = sut.getContent().collect(Collectors.toList());

        // Then
        assertThat(actual).hasSize(3);
    }

}
