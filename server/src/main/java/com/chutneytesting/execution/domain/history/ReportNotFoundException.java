package com.chutneytesting.execution.domain.history;

@SuppressWarnings("serial")
public class ReportNotFoundException extends RuntimeException {

    public ReportNotFoundException(String scenarioId, Long reportId) {
        super("Unable to find report " + reportId + " of scenario " + scenarioId);
    }

    public ReportNotFoundException(String scenarioId) {
        super("No report available for scenario " + scenarioId);
    }
}
