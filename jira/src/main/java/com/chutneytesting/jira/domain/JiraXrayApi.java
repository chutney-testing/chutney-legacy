package com.chutneytesting.jira.domain;

import com.chutneytesting.jira.infra.xraymodelapi.Xray;
import com.chutneytesting.jira.infra.xraymodelapi.XrayTestExecTest;
import java.util.List;

public interface JiraXrayApi {

    void updateRequest(Xray xray, JiraTargetConfiguration jiraTargetConfiguration);

    List<XrayTestExecTest> getTestExecutionScenarios(String testExecutionId, JiraTargetConfiguration jiraTargetConfiguration);

    void updateStatusByTestRunId(String id, String executionStatus, JiraTargetConfiguration jiraTargetConfiguration);
}
