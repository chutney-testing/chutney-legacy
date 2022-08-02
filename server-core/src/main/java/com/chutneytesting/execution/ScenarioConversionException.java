package com.chutneytesting.execution;

@SuppressWarnings("serial")
public class ScenarioConversionException extends RuntimeException {

    public ScenarioConversionException(String scenarioId, Exception e) {
        super("Unable to convert scenario [" + scenarioId + "]: " + e.getMessage(), e);
    }

    public ScenarioConversionException(String scenarioId, String message) {
        super("Unable to convert scenario [" + scenarioId + "]: " + message);
    }

    public ScenarioConversionException(Exception e) {
        super("Unable to convert scenario: " + e.getMessage(), e);
    }
}
