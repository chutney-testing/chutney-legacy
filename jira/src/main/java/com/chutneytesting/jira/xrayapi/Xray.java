package com.chutneytesting.jira.xrayapi;

import java.util.List;

public class Xray {
    private String testExecutionKey;
    private List<XrayTest> tests;
    private XrayInfo info;

    public Xray(String testExecutionKey, List<XrayTest> tests, XrayInfo info) {
        this.testExecutionKey = testExecutionKey;
        this.tests = tests;
        this.info = info;
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

    public XrayInfo getInfo() {
        return info;
    }

    public void setInfo(XrayInfo info) {
        this.info = info;
    }
}
