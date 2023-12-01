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

package com.chutneytesting.execution.api.report.surefire;

import com.chutneytesting.execution.api.report.surefire.Testsuite.Properties;
import com.chutneytesting.execution.api.report.surefire.Testsuite.Testcase;
import com.chutneytesting.execution.api.report.surefire.Testsuite.Testcase.Failure;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.execution.report.StepExecutionReportCore;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionCampaign;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class SurefireScenarioExecutionReportBuilder {

    private final ObjectFactory objectFactory = new ObjectFactory();
    private final ObjectMapper objectMapper;
    private final ExecutionHistoryRepository executionHistoryRepository;

    SurefireScenarioExecutionReportBuilder(ObjectMapper objectMapper, ExecutionHistoryRepository executionHistoryRepository) {
        this.objectMapper = objectMapper; // TODO - Choose explicitly which mapper to use
        this.executionHistoryRepository = executionHistoryRepository;
    }

    Testsuite create(ScenarioExecutionCampaign scenarioExecutionReport) {
        Testsuite testsuite = objectFactory.createTestsuite();
        testsuite.setName(scenarioExecutionReport.scenarioId + "_" + scenarioExecutionReport.scenarioName);
        testsuite.setTime(toSurefireDuration(scenarioExecutionReport.execution.duration()));

        String rawReport = executionHistoryRepository.getExecution(scenarioExecutionReport.scenarioId, scenarioExecutionReport.execution.executionId()).report();
        ScenarioExecutionReport executionReport = parse(rawReport);

        populateTestsuite(testsuite, executionReport);

        return testsuite;
    }

    private ScenarioExecutionReport parse(String rawReport) {
        try {
            return objectMapper.readerFor(ScenarioExecutionReport.class).readValue(rawReport);
        } catch (IOException e) {
            throw new UncheckedIOException(e.getMessage(), e);
        }
    }

    private void populateTestsuite(Testsuite testsuite, ScenarioExecutionReport executionReport) {
        Properties properties = new Properties();
        testsuite.getProperties().add(properties);

        AtomicInteger testCounter = new AtomicInteger();
        AtomicInteger failureCounter = new AtomicInteger();
        AtomicInteger skippedCounter = new AtomicInteger();
        attachTestCases(testsuite, testCounter, failureCounter, skippedCounter, "", "", executionReport.report.steps);

        testsuite.setTests(String.valueOf(testCounter.get()));
        testsuite.setFailures(String.valueOf(failureCounter.get()));
        testsuite.setSkipped(String.valueOf(skippedCounter.get()));
        testsuite.setErrors("0");
    }

    private void attachTestCases(Testsuite testsuite, AtomicInteger testCounter, AtomicInteger failureCounter, AtomicInteger skippedCounter, String stepIndexPrefix, String stepNamePrefix, List<StepExecutionReportCore> steps) {
        AtomicInteger stepIndex = new AtomicInteger();
        steps.forEach(stepExecutionReport -> {
            String currentStepIndexPrefix = stepIndexPrefix + stepIndex.incrementAndGet();
            testCounter.incrementAndGet();
            Testcase testcase = new Testcase();
            String stepName = Strings.isNullOrEmpty(stepExecutionReport.name) ? "<no name>" : stepExecutionReport.name;
            String testcaseName = stepNamePrefix + stepName;
            testcase.setName(currentStepIndexPrefix + " - " + testcaseName);
            testcase.setTime(toSurefireDuration(stepExecutionReport.duration));
            if (ServerReportStatus.FAILURE == stepExecutionReport.status) {
                failureCounter.incrementAndGet();
                stepExecutionReport.errors.forEach(error -> {
                    Failure failure = objectFactory.createTestsuiteTestcaseFailure();
                    failure.setMessage(error);
                    testcase.getFailure().add(failure);
                });
            } else if (ServerReportStatus.NOT_EXECUTED == stepExecutionReport.status) {
                skippedCounter.incrementAndGet();
                Testsuite.Testcase.Skipped skipped = new Testsuite.Testcase.Skipped();
                skipped.setMessage("Not executed");
                testcase.setSkipped(objectFactory.createTestsuiteTestcaseSkipped(skipped));
            }
            if (!stepExecutionReport.information.isEmpty()) {
                testcase.setSystemOut(objectFactory.createTestsuiteTestcaseSystemOut(toSurefireLabel(stepExecutionReport.information)));
            }

            testsuite.getTestcase().add(testcase);
            attachTestCases(testsuite, testCounter, failureCounter, skippedCounter, currentStepIndexPrefix + ".", testcaseName + " / ", stepExecutionReport.steps);
        });
    }

    private String toSurefireDuration(long duration) {
        return String.valueOf((double) duration / 1000);
    }

    private String toSurefireLabel(Collection<String> labels) {
        return String.join(", ", labels);
    }
}
