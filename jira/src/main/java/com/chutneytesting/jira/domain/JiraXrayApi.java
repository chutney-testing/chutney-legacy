package com.chutneytesting.jira.domain;

import com.chutneytesting.jira.infra.xraymodelapi.Xray;
import java.util.List;

public interface JiraXrayApi {

    void updateRequest(Xray xray, JiraTargetConfiguration jiraTargetConfiguration);

    List<String> getTestExecutionScenarios(String testExecutionId, JiraTargetConfiguration jiraTargetConfiguration);
}
