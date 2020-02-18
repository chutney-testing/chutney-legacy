package com.chutneytesting.instrument.domain;

import com.chutneytesting.execution.domain.report.ServerReportStatus;
import java.util.Collection;

public interface ChutneyMetrics {

    void onNewScenario(String scenarioTitle, Collection<String> tags);

    void onScenarioChange(String scenarioTitle, Collection<String> tags);

    void onExecutionEnded(String scenarioTitle, ServerReportStatus status, long duration);
}
