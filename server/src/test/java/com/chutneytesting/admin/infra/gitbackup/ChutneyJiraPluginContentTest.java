package com.chutneytesting.admin.infra.gitbackup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.design.domain.plugins.jira.JiraRepository;
import com.chutneytesting.design.domain.plugins.jira.JiraTargetConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ChutneyJiraPluginContentTest {

    @TempDir
    static Path temporaryFolder;

    private ObjectMapper objectMapperMock;
    private ChutneyJiraPluginContent sut;
    private JiraRepository repositoryMock;

    @BeforeEach
    void beforeEach() {
        objectMapperMock = mock(ObjectMapper.class);
        repositoryMock = mock(JiraRepository.class);
    }

    @Test
    void should_return_jira_plugin_content() {
        // Given
        when(repositoryMock.loadServerConfiguration())
            .thenReturn(new JiraTargetConfiguration("url", "username", "password"));

        when(repositoryMock.getAllLinkedScenarios())
            .thenReturn(Map.of("42", "4242"));

        when(repositoryMock.getAllLinkedCampaigns())
            .thenReturn(Map.of(
            "13", "37"
        ));

        // When
        sut = new ChutneyJiraPluginContent(repositoryMock, objectMapperMock);
        List<ChutneyContent> actual = sut.getContent().collect(Collectors.toList());

        // Then
        assertThat(actual).hasSize(3);
    }

    @Test
    void exception_should_not_interrupt_importing_other_files() throws IOException {
        // Given
        Path config = temporaryFolder.resolve("jira_config.json");
        Files.write(config, "".getBytes());

        Path scenarios = temporaryFolder.resolve("scenario_link.json");
        Files.write(scenarios, "".getBytes());

        Path campaigns = temporaryFolder.resolve("campaign_link.json");
        Files.write(campaigns, "".getBytes());

        when(objectMapperMock.readValue(any(byte[].class), eq(Map.class)))
            .thenThrow(new IOException());

        // When
        sut = new ChutneyJiraPluginContent(repositoryMock, objectMapperMock);

        // Then
        assertThrows(RuntimeException.class, () -> /*When*/ sut.importFolder(temporaryFolder));
    }
}
