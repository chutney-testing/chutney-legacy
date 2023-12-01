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

package com.chutneytesting.execution.infra.storage;

import static com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory.ExecutionSummary.Builder;
import static com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory.ExecutionSummary.builder;
import static java.util.Optional.ofNullable;

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.ExecutionSummary;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;

class ExecutionSummaryRowMapper implements RowMapper<ExecutionSummary> {

    @Override
    public ExecutionSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
        Builder builder = builder()
            .executionId(rs.getLong("ID"))
            .time(Instant.ofEpochMilli(rs.getLong("EXECUTION_TIME")).atZone(ZoneId.systemDefault()).toLocalDateTime())
            .duration(rs.getLong("DURATION"))
            .status(ServerReportStatus.valueOf(rs.getString("STATUS")))
            .info(ofNullable(rs.getString("INFORMATION")))
            .error(ofNullable(rs.getString("ERROR")))
            .testCaseTitle(rs.getString("TEST_CASE_TITLE"))
            .environment((rs.getString("ENVIRONMENT")))
            .datasetId(ofNullable(rs.getString("DATASET_ID")))
            .datasetVersion(ofNullable(rs.getString("DATASET_VERSION")).map(Integer::valueOf))
            .user((rs.getString("USER_ID")));
        addCampaignReportIfExist(rs, builder);
        return builder.build();
    }

    private void addCampaignReportIfExist(ResultSet rs, Builder builder) {
        try {
            if (rs.getLong("CAMPAIGN_EXECUTION_ID") > 0) {
                builder.campaignReport(mapCampaignExecutionReport(rs));
            }
        } catch (SQLException e) {
        }
    }

    private Optional<CampaignExecution> mapCampaignExecutionReport(ResultSet rs) throws SQLException {
        CampaignExecution report = new CampaignExecution(
            rs.getLong("CAMPAIGN_EXECUTION_ID"),
            rs.getLong("CAMPAIGN_ID"),
            new ArrayList<>(),
            rs.getString("CAMPAIGN_TITLE"),
            false,
            null,
            null,
            null,
            null);
        return Optional.of(report);
    }
}
