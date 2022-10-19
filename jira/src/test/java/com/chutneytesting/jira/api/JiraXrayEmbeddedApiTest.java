package com.chutneytesting.jira.api;

import static com.chutneytesting.jira.domain.XrayStatus.FAIL;
import static com.chutneytesting.jira.domain.XrayStatus.PASS;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.jira.domain.JiraRepository;
import com.chutneytesting.jira.domain.JiraTargetConfiguration;
import com.chutneytesting.jira.domain.JiraXrayApi;
import com.chutneytesting.jira.domain.JiraXrayClientFactory;
import com.chutneytesting.jira.domain.JiraXrayService;
import com.chutneytesting.jira.infra.JiraFileRepository;
import com.chutneytesting.jira.xrayapi.Xray;
import com.chutneytesting.jira.xrayapi.XrayTest;
import com.chutneytesting.jira.xrayapi.XrayTestExecTest;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class JiraXrayEmbeddedApiTest {

    private final JiraXrayApi jiraXrayApiMock = mock(JiraXrayApi.class);
    private final JiraXrayClientFactory jiraXrayFactory = mock(JiraXrayClientFactory.class);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
    private final JiraTargetConfiguration jiraTargetConfiguration = new JiraTargetConfiguration("an url", "a username", "a password");

    private JiraXrayEmbeddedApi jiraXrayEmbeddedApi;
    private JiraRepository jiraRepository;

    @BeforeEach
    public void setUp() throws IOException {
        jiraRepository = new JiraFileRepository(Files.createTempDirectory("jira").toString());
        jiraRepository.saveServerConfiguration(jiraTargetConfiguration);

        JiraXrayService jiraXrayService = new JiraXrayService(jiraRepository, jiraXrayFactory);

        when(jiraXrayFactory.create(any())).thenReturn(jiraXrayApiMock);

        jiraXrayEmbeddedApi = new JiraXrayEmbeddedApi(jiraXrayService);
    }

    @Test
    void getTestStatus() {
        List<XrayTestExecTest> result = new ArrayList<>();
        XrayTestExecTest xrayTestExecTest = new XrayTestExecTest();
        xrayTestExecTest.setId("12345");
        xrayTestExecTest.setKey("SCE-2");
        xrayTestExecTest.setStatus(PASS.value);
        result.add(xrayTestExecTest);

        XrayTestExecTest xrayTestExecTest2 = new XrayTestExecTest();
        xrayTestExecTest2.setId("123456");
        xrayTestExecTest2.setKey("SCE-1");
        xrayTestExecTest2.setStatus(FAIL.value);
        result.add(xrayTestExecTest2);

        when(jiraXrayApiMock.getTestExecutionScenarios(anyString())).thenReturn(result);

        List<XrayTestExecTest> statusByTest = jiraXrayEmbeddedApi.getTestStatusInTestExec("");
        assertThat(statusByTest.get(0).getStatus()).isEqualTo(PASS.value);
        assertThat(statusByTest.get(1).getStatus()).isEqualTo(FAIL.value);
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
        jiraXrayEmbeddedApi.updateTestExecution(20L, 1L, "1", report);

        //T
        ArgumentCaptor<Xray> xrayArgumentCaptor = ArgumentCaptor.forClass(Xray.class);
        verify(jiraXrayApiMock, times(1)).updateRequest(xrayArgumentCaptor.capture());

        Xray xrayValue = xrayArgumentCaptor.getValue();
        XrayTest xrayTest = xrayValue.getTests().get(0);
        assertThat(xrayTest.getTestKey()).isEqualTo("SCE-1");
        assertThat(xrayTest.getStart()).isEqualTo(Instant.parse("2021-05-19T11:22:33.00Z").atZone(ZoneId.systemDefault()).format(formatter));
        assertThat(xrayTest.getFinish()).isEqualTo(Instant.parse("2021-05-19T11:22:43.00Z").atZone(ZoneId.systemDefault()).format(formatter));
        assertThat(xrayTest.getComment()).isEqualTo("[ > rootStep > sub step => [Sub step error 1, Sub step error 2],  > rootStep => [Root error]]");
        assertThat(xrayTest.getStatus()).isEqualTo(PASS.value);
    }

    @Test
    @DisplayName("Given an execution report, When we want to send the result to jira xray using test plan id, Then new test execution are created")
    void updateTestExecutionWithTestPlan() {
        // G
        jiraRepository.saveForCampaign("20", "JIRA-20");
        jiraRepository.saveForScenario("1", "SCE-1");
        ReportForJira.Step subStep = new ReportForJira.Step("sub step", of("Sub step error 1", "Sub step error 2"), null);
        ReportForJira.Step rootStep = new ReportForJira.Step("rootStep", of("Root error"), of(subStep));
        ReportForJira report = new ReportForJira(Instant.parse("2021-05-19T11:22:33.00Z"), 10000L, "SUCCESS", rootStep, "env");

        //W
        when(jiraXrayApiMock.isTestPlan("JIRA-20")).thenReturn(true);
        when(jiraXrayApiMock.createTestExecution("JIRA-20")).thenReturn("JIRA-22");
        jiraXrayEmbeddedApi.updateTestExecution(20L, 1L, "1", report);

        //T
        ArgumentCaptor<Xray> xrayArgumentCaptor = ArgumentCaptor.forClass(Xray.class);
        verify(jiraXrayApiMock, times(1)).updateRequest(xrayArgumentCaptor.capture());

        Xray xrayValue = xrayArgumentCaptor.getValue();
        assertThat(xrayValue.getTestExecutionKey()).isEqualTo("JIRA-22");
        jiraRepository.getByCampaignExecutionId("1").equals("JIRA-22");
    }
}
