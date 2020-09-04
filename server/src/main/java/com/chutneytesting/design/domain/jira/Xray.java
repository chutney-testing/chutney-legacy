package com.chutneytesting.design.domain.jira;

import java.util.List;

public class Xray {
    private String testExecutionKey;
    private List<XrayTest> tests;

    public Xray(String testExecutionKey, List<XrayTest> tests) {
        this.testExecutionKey = testExecutionKey;
        this.tests = tests;
    }

    public String getTestExecutionKey() {
        return testExecutionKey;
    }

    public void setTestExecutionKey(String testExecutionKey) {
        this.testExecutionKey = testExecutionKey;
    }

    public List<XrayTest> getTests() {
        return tests;
    }

    public void setTests(List<XrayTest> tests) {
        this.tests = tests;
    }
}
