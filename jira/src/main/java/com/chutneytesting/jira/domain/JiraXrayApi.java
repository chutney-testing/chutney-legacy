package com.chutneytesting.jira.domain;

import com.chutneytesting.jira.xrayapi.Xray;
import com.chutneytesting.jira.xrayapi.XrayTestExecTest;
import java.util.List;

public interface JiraXrayApi {

    void updateRequest(Xray xray);

    List<XrayTestExecTest> getTestExecutionScenarios(String xrayId);

    void updateStatusByTestRunId(String id, String executionStatus);

    void associateTestExecutionFromTestPlan(String testPlanId, String testExecutionId);

    String createTestExecution(String testPlanId);

    boolean isTestPlan(String issueId);

}
