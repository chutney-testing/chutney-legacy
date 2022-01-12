package com.chutneytesting.jira.domain;

import com.chutneytesting.jira.xray_api.Xray;
import com.chutneytesting.jira.xray_api.XrayTestExecTest;
import java.util.List;

public interface JiraXrayApi {

    void updateRequest(Xray xray, JiraTargetConfiguration jiraTargetConfiguration);

    List<XrayTestExecTest> getTestExecutionScenarios(String testExecutionId, JiraTargetConfiguration jiraTargetConfiguration);

    void updateStatusByTestRunId(String id, String executionStatus, JiraTargetConfiguration jiraTargetConfiguration);
}
