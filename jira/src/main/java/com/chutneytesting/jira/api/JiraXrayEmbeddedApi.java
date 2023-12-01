/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.jira.api;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.chutneytesting.jira.domain.JiraXrayService;
import com.chutneytesting.jira.xrayapi.XrayTestExecTest;
import java.util.List;

public class JiraXrayEmbeddedApi {

    private final JiraXrayService jiraXrayService;

    public JiraXrayEmbeddedApi(JiraXrayService jiraXrayService) {
        this.jiraXrayService = jiraXrayService;
    }

    public void updateTestExecution(Long campaignId, Long campaignExecutionId, String scenarioId, ReportForJira report) {
        if (report != null && isNotEmpty(scenarioId) && campaignId != null) {
            jiraXrayService.updateTestExecution(campaignId, campaignExecutionId, scenarioId, report);
        }
    }

    public List<XrayTestExecTest> getTestStatusInTestExec(String testExec) { // TODO - Only used in a test ?
        return jiraXrayService.getTestExecutionScenarios(testExec);
    }
}


