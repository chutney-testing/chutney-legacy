package com.chutneytesting.jira.xray_api;

import java.util.List;

public class XrayInfo {
    private String summary;
    private String description;
    private String version;
    private String user;
    private String revision;
    private String startDate;
    private String finishDate;
    private String testPlanKey;
    private List<String> testEnvironments;

    public XrayInfo(String summary, String description, String version, String user, String revision, String startDate, String finishDate, String testPlanKey, List<String> testEnvironments) {
        this.summary = summary;
        this.description = description;
        this.version = version;
        this.user = user;
        this.revision = revision;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.testPlanKey = testPlanKey;
        this.testEnvironments = testEnvironments;
    }

    public XrayInfo(List<String> testEnvironments) {
        this.testEnvironments = testEnvironments;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(String finishDate) {
        this.finishDate = finishDate;
    }

    public String getTestPlanKey() {
        return testPlanKey;
    }

    public void setTestPlanKey(String testPlanKey) {
        this.testPlanKey = testPlanKey;
    }

    public List<String> getTestEnvironments() {
        return testEnvironments;
    }

    public void setTestEnvironments(List<String> testEnvironments) {
        this.testEnvironments = testEnvironments;
    }
}
