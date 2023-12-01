/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
