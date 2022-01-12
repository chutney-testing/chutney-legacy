package com.chutneytesting.jira.xray_api;

import java.util.List;

public class XrayTest {
    private String testKey;
    private String start;
    private String finish;
    private String comment;
    private String status;
    private List<XrayEvidence> evidences;

    public XrayTest(String testKey, String start, String finish, String comment, String status, List<XrayEvidence> evidences) {
        this.testKey = testKey;
        this.start = start;
        this.finish = finish;
        this.comment = comment;
        this.status = status;
        this.evidences = evidences;
    }

    public XrayTest(String testKey, String start, String finish, String comment, String status) {
        this.testKey = testKey;
        this.start = start;
        this.finish = finish;
        this.comment = comment;
        this.status = status;
    }

    public String getTestKey() {
        return testKey;
    }

    public void setTestKey(String testKey) {
        this.testKey = testKey;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getFinish() {
        return finish;
    }

    public void setFinish(String finish) {
        this.finish = finish;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<XrayEvidence> getEvidences() {
        return evidences;
    }

    public void setEvidences(List<XrayEvidence> evidences) {
        this.evidences = evidences;
    }
}
