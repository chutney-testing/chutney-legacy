package com.chutneytesting.jira.api;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.chutneytesting.jira.domain.JiraRepository;
import com.chutneytesting.jira.domain.JiraTargetConfiguration;
import com.chutneytesting.jira.domain.JiraXrayApi;
import com.chutneytesting.jira.domain.JiraXrayService;
import com.chutneytesting.jira.infra.JiraFileRepository;
import com.chutneytesting.jira.infra.xraymodelapi.Xray;
import com.chutneytesting.jira.infra.xraymodelapi.XrayTest;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class JiraXrayEmbeddedApiTest {

    private JiraXrayEmbeddedApi jiraXrayEmbeddedApi;
    private final JiraXrayApi jiraXrayApiMock = mock(JiraXrayApi.class);
    private JiraRepository jiraRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
    private final JiraTargetConfiguration jiraTargetConfiguration = new JiraTargetConfiguration("an url", "a username", "a password");

    @BeforeEach
    public void setUp() throws IOException {
        jiraRepository = new JiraFileRepository(Files.createTempDirectory("jira").toString());
        jiraRepository.saveServerConfiguration(jiraTargetConfiguration);
        JiraXrayService jiraXrayService = new JiraXrayService(jiraRepository, jiraXrayApiMock);
        jiraXrayEmbeddedApi = new JiraXrayEmbeddedApi(jiraXrayService);
    }

    @Test
    @DisplayName("Given an execution report, When we want to send the result to jira xray, Then the xray model api is filled with information from the report")
    void updateTestExecution() {
        // G
        jiraRepository.saveForCampaign("20", "JIRA-20");
        jiraRepository.saveForScenario("1", "SCE-1");
        ReportForJira.Step subStep = new ReportForJira.Step("sub step", of("Sub step error 1", "Sub step error 2"), null);
        ReportForJira.Step rootStep = new ReportForJira.Step("rootStep", of("Root error"), of(subStep));
        ReportForJira report = new ReportForJira(Instant.parse("2021-05-19T11:22:33.00Z"), 10000L, "SUCCESS", rootStep, "env");

        //W
        jiraXrayEmbeddedApi.updateTestExecution(20L, "1", report);

        //T
        ArgumentCaptor<Xray> xrayArgumentCaptor = ArgumentCaptor.forClass(Xray.class);
        ArgumentCaptor<JiraTargetConfiguration> jiraTargetConfigurationArgumentCaptor = ArgumentCaptor.forClass(JiraTargetConfiguration.class);
        verify(jiraXrayApiMock, times(1)).updateRequest(xrayArgumentCaptor.capture(), jiraTargetConfigurationArgumentCaptor.capture());

        JiraTargetConfiguration configurationValue = jiraTargetConfigurationArgumentCaptor.getValue();
        assertThat(jiraTargetConfiguration).usingRecursiveComparison().isEqualTo(configurationValue);

        Xray xrayValue = xrayArgumentCaptor.getValue();
        XrayTest xrayTest = xrayValue.getTests().get(0);
        assertThat(xrayTest.getTestKey()).isEqualTo("SCE-1");
        assertThat(xrayTest.getStart()).isEqualTo(Instant.parse("2021-05-19T11:22:33.00Z").atZone(ZoneId.systemDefault()).format(formatter));
        assertThat(xrayTest.getFinish()).isEqualTo(Instant.parse("2021-05-19T11:22:43.00Z").atZone(ZoneId.systemDefault()).format(formatter));
        assertThat(xrayTest.getComment()).isEqualTo("[ > rootStep > sub step => [Sub step error 1, Sub step error 2],  > rootStep => [Root error]]");
        assertThat(xrayTest.getStatus()).isEqualTo("PASS");
    }
}
