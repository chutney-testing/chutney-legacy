package com.chutneytesting.jira.domain;


import static com.chutneytesting.jira.domain.XrayStatus.FAIL;
import static com.chutneytesting.jira.domain.XrayStatus.PASS;
import static java.util.Collections.emptyList;

import com.chutneytesting.jira.api.ReportForJira;
import com.chutneytesting.jira.domain.exception.NoJiraConfigurationException;
import com.chutneytesting.jira.infra.HttpJiraXrayImpl;
import com.chutneytesting.jira.xrayapi.Xray;
import com.chutneytesting.jira.xrayapi.XrayEvidence;
import com.chutneytesting.jira.xrayapi.XrayInfo;
import com.chutneytesting.jira.xrayapi.XrayTest;
import com.chutneytesting.jira.xrayapi.XrayTestExecTest;
import com.google.common.annotations.VisibleForTesting;
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

    private final JiraRepository jiraRepository;

    public JiraXrayService(JiraRepository jiraRepository) {
        this.jiraRepository = jiraRepository;

    }

    public void updateTestExecution(Long campaignId, String scenarioId, ReportForJira report) {
        JiraXrayApi jiraXrayApi = createHttpJiraXrayImpl();

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
                report.status.equals("SUCCESS") ? PASS.value : FAIL.value
            );

            xrayTest.setEvidences(getEvidences(report.rootStep, ""));
            XrayInfo info = new XrayInfo(Collections.singletonList(report.environment));
            Xray xray = new Xray(testExecutionKey, Collections.singletonList(xrayTest), info);
            jiraXrayApi.updateRequest(xray);
        }
    }

    public List<XrayTestExecTest> getTestExecutionScenarios(String testExecutionId) {
        JiraXrayApi jiraXrayApi = createHttpJiraXrayImpl();

        return jiraXrayApi.getTestExecutionScenarios(testExecutionId);
    }

    public void updateScenarioStatus(String testExecId, String chutneyId, String executionStatus) {
        JiraXrayApi jiraXrayApi = createHttpJiraXrayImpl();

        String scenarioJiraId = jiraRepository.getByScenarioId(chutneyId);

        List<XrayTestExecTest> testExecutionScenarios = getTestExecutionScenarios(testExecId);
        Optional<XrayTestExecTest> foundTest = testExecutionScenarios.stream().filter(test -> scenarioJiraId.equals(test.getKey())).findFirst();
        if (foundTest.isPresent()) {
            jiraXrayApi.updateStatusByTestRunId(foundTest.get().getId(), executionStatus);
        }
    }

    @VisibleForTesting
    JiraXrayApi createHttpJiraXrayImpl() {
        JiraTargetConfiguration jiraTargetConfiguration = jiraRepository.loadServerConfiguration();
        if (jiraTargetConfiguration.isValid()) {
            LOGGER.error("Unable to update xray, jira url is undefined");
            throw new NoJiraConfigurationException();
        } else {
            return new HttpJiraXrayImpl(jiraTargetConfiguration);
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
