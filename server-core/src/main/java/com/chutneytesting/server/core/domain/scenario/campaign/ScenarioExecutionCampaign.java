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
