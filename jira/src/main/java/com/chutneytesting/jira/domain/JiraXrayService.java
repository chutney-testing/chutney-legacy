package com.chutneytesting.jira.domain;


import static java.util.Collections.emptyList;

import com.chutneytesting.jira.api.ReportForJira;
import com.chutneytesting.jira.infra.xraymodelapi.Xray;
import com.chutneytesting.jira.infra.xraymodelapi.XrayEvidence;
import com.chutneytesting.jira.infra.xraymodelapi.XrayInfo;
import com.chutneytesting.jira.infra.xraymodelapi.XrayTest;
import com.chutneytesting.jira.infra.xraymodelapi.XrayTestExecTest;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JiraXrayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraXrayService.class);
    private static final String SUCCESS_STATUS = "PASS";
    private static final String FAILED_STATUS = "FAIL";

    private final JiraRepository jiraRepository;
    private final JiraXrayApi jiraXrayApi;

    public JiraXrayService(JiraRepository jiraRepository, JiraXrayApi jiraXrayApi) {
        this.jiraRepository = jiraRepository;
        this.jiraXrayApi = jiraXrayApi;
    }

    public void updateTestExecution(Long campaignId, String scenarioId, ReportForJira report) {
        String testKey = jiraRepository.getByScenarioId(scenarioId);
        String testExecutionKey = jiraRepository.getByCampaignId(campaignId.toString());
        if (!testKey.isEmpty() && !testExecutionKey.isEmpty()) {
            LOGGER.info("Update xray test {} of test execution {}", testKey, testExecutionKey);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
            XrayTest xrayTest = new XrayTest(
                testKey,
                report.startDate.atZone(ZoneId.systemDefault()).format(formatter),
                report.startDate.plusNanos(report.duration * 1000000).atZone(ZoneId.systemDefault()).format(formatter),
                getErrors(report).toString(),
                report.status.equals("SUCCESS") ? SUCCESS_STATUS : FAILED_STATUS
            );

            xrayTest.setEvidences(getEvidences(report.rootStep, ""));
            XrayInfo info = new XrayInfo(Collections.singletonList(report.environment));
            Xray xray = new Xray(testExecutionKey, Collections.singletonList(xrayTest), info);
            JiraTargetConfiguration jiraTargetConfiguration = jiraRepository.loadServerConfiguration();
            jiraXrayApi.updateRequest(xray, jiraTargetConfiguration);
        }
    }

    public List<XrayTestExecTest> getTestExecutionScenarios(String testExecutionId) {
        JiraTargetConfiguration jiraTargetConfiguration = jiraRepository.loadServerConfiguration();
        if (jiraTargetConfiguration.url.isEmpty()) {
            return emptyList();
        }
        return jiraXrayApi.getTestExecutionScenarios(testExecutionId, jiraTargetConfiguration);
    }

    public void updateScenarioStatus(String testExecId, String chutneyId, String executionStatus) {
        JiraTargetConfiguration jiraTargetConfiguration = jiraRepository.loadServerConfiguration();
        if (jiraTargetConfiguration.url.isEmpty()) {
            return;
        }
        String scenarioJiraId = jiraRepository.getByScenarioId(chutneyId);

        List<XrayTestExecTest> testExecutionScenarios = getTestExecutionScenarios(testExecId);
        Optional<XrayTestExecTest> foundTest = testExecutionScenarios.stream().filter(test -> scenarioJiraId.equals(test.getKey())).findFirst();
        if(foundTest.isPresent()) {
            jiraXrayApi.updateStatusByTestRunId(foundTest.get().getId(), executionStatus, jiraTargetConfiguration);
        }
    }

    private List<String> getErrors(ReportForJira report) {
        List<String> errors = new ArrayList<>();
        getErrors(report.rootStep, "").forEach((k, v) -> errors.add(k + " => " + v));
        return errors;
    }

    private Map<String, String> getErrors(ReportForJira.Step currentStep, String parentStep) {
        Map<String, String> errors = new HashMap<>();
        if (!currentStep.errors.isEmpty()) {
            errors.put(parentStep + " > " + currentStep.name,
                currentStep.errors.stream().filter(s -> !s.startsWith("data:image/png")).collect(Collectors.toList()).toString());
        }
        if (!currentStep.steps.isEmpty()) {
            currentStep.steps
                .forEach(subStep -> errors.putAll(getErrors(subStep, parentStep + " > " + currentStep.name)));
        }
        return errors;
    }

    private List<XrayEvidence> getEvidences(ReportForJira.Step currentStep, String parentStep) {
        List<XrayEvidence> evidences = new ArrayList<>();
        if (!currentStep.errors.isEmpty()) {
            evidences.addAll(
                currentStep.errors
                    .stream()
                    .filter(s -> s.startsWith("data:image/png"))
                    .map(s -> new XrayEvidence(s.replace("data:image/png;base64,", ""), formatEvidenceFilename(parentStep, currentStep.name) + ".png", "image/png"))
                    .collect(Collectors.toList())
            );
        }
        if (!currentStep.steps.isEmpty()) {
            currentStep.steps
                .forEach(subStep -> evidences.addAll(getEvidences(subStep, formatEvidenceFilename(parentStep, currentStep.name))));
        }
        return evidences;
    }

    private String formatEvidenceFilename(String parentStep, String stepName) {
        return parentStep.trim().replace(" ", "-")
            + (parentStep.trim().isEmpty() ? "" : "_")
            + stepName.trim().replace(" ", "-");
    }
}
