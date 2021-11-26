package com.chutneytesting.jira.api;

<<<<<<< HEAD
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.chutneytesting.jira.domain.JiraXrayService;
=======
import com.chutneytesting.jira.domain.JiraXrayService;
import java.util.List;
>>>>>>> 6c4f98ca (chore(server+jira): Create a new maven module for jira features)

public class JiraXrayEmbeddedApi {

    private final JiraXrayService jiraXrayService;

    public JiraXrayEmbeddedApi(JiraXrayService jiraXrayService) {
        this.jiraXrayService = jiraXrayService;
    }

    public void updateTestExecution(Long campaignId, String scenarioId, ReportForJira report) {
<<<<<<< HEAD
        if (report != null && isNotEmpty(scenarioId) && campaignId != null) {
            jiraXrayService.updateTestExecution(campaignId, scenarioId, report);
        }
=======
        jiraXrayService.updateTestExecution(campaignId, scenarioId, report);
>>>>>>> 6c4f98ca (chore(server+jira): Create a new maven module for jira features)
    }
}


