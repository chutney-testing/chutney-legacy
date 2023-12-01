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

package com.chutneytesting.server.core.domain.scenario.campaign;

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import java.util.Comparator;
import java.util.Objects;

public class ScenarioExecutionCampaign {
    public final String scenarioId;
    public final String scenarioName;
    public final ExecutionHistory.ExecutionSummary execution;

    public ScenarioExecutionCampaign(String scenarioId,
                                     String scenarioName,
                                     ExecutionHistory.ExecutionSummary execution) {
        this.scenarioId = scenarioId;
        this.scenarioName = scenarioName;
        this.execution = execution;
    }

    public ServerReportStatus status() {
        return execution.status();
    }

    public static Comparator<ScenarioExecutionCampaign> executionIdComparator() {
        return Comparator.comparingLong(value -> value.execution.executionId() > 0 ? value.execution.executionId() : Long.MAX_VALUE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScenarioExecutionCampaign that = (ScenarioExecutionCampaign) o;
        return scenarioId.equals(that.scenarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenarioId);
    }
}
