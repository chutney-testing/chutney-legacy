package com.chutneytesting.instrument.domain;

import com.chutneytesting.execution.domain.report.ServerReportStatus;
import java.util.Collection;

public interface Metrics {

    void onNewScenario(String scenarioTitle, Collection<String> tags);

    void onScenarioChange(String scenarioTitle, Collection<String> tags);

    void onExecutionEnded(String scenarioTitle, ServerReportStatus status, long duration);
}
