package com.chutneytesting.server.core.scenario;

@SuppressWarnings("serial")
public class ScenarioNotParsableException extends RuntimeException {

    public ScenarioNotParsableException(String identifier, Exception e) {
        super("TestCase [" + identifier + "] is not valid: " + e.getMessage());
    }

}