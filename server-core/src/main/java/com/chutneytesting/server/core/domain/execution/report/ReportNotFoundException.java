package com.chutneytesting.server.core.domain.execution.report;

@SuppressWarnings("serial")
public class ReportNotFoundException extends RuntimeException {

    public ReportNotFoundException(String scenarioId, Long reportId) {
        super("Unable to find report " + reportId + " of scenario " + scenarioId);
    }

    public ReportNotFoundException(Long reportId) {
        super("Unable to find report " + reportId);
    }
}
