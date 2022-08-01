package com.chutneytesting.campaign.domain;

import com.chutneytesting.execution.domain.ServerReportStatus;
import com.chutneytesting.execution.domain.history.ExecutionHistory;
import java.util.Comparator;
import java.util.Objects;

public class ScenarioExecutionReportCampaign {
    public final String scenarioId;
    public final String scenarioName;
    public final ExecutionHistory.ExecutionSummary execution;

    public ScenarioExecutionReportCampaign(String scenarioId,
                                           String scenarioName,
                                           ExecutionHistory.ExecutionSummary execution) {
        this.scenarioId = scenarioId;
        this.scenarioName = scenarioName;
        this.execution = execution;
    }

    public ServerReportStatus status() {
        return execution.status();
    }

    public static Comparator<ScenarioExecutionReportCampaign> executionIdComparator() {
        return Comparator.comparingLong(value -> value.execution.executionId() > 0 ? value.execution.executionId() : Long.MAX_VALUE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScenarioExecutionReportCampaign that = (ScenarioExecutionReportCampaign) o;
        return scenarioId.equals(that.scenarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenarioId);
    }
}
