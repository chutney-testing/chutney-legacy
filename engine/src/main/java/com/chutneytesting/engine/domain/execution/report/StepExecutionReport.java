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

package com.chutneytesting.engine.domain.execution.report;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class StepExecutionReport implements Status.HavingStatus {

    public final Long executionId;
    public final String name;
    public final String environment;
    public final Long duration;
    public final Instant startDate;
    public final Status status;
    public final List<String> information;
    public final List<String> errors;
    public final List<StepExecutionReport> steps;
    public final String type;
    public final String targetName;
    public final String targetUrl;
    public final String strategy;
    public final Map<String, Object> evaluatedInputs;

    @JsonIgnore
    public Map<String, Object> stepResults;
    @JsonIgnore
    public Map<String, Object> scenarioContext;

    @JsonCreator
    public StepExecutionReport(Long executionId,
                               String name,
                               String environment,
                               Long duration,
                               Instant startDate,
                               Status status,
                               List<String> information,
                               List<String> errors,
                               List<StepExecutionReport> steps,
                               String type,
                               String targetName,
                               String targetUrl,
                               String strategy
    ) {
        this(executionId, name, environment, duration, startDate, status, information, errors, steps, type, targetName, targetUrl, strategy, null, null, null);
    }

    public StepExecutionReport(Long executionId,
                               String name,
                               String environment,
                               Long duration,
                               Instant startDate,
                               Status status,
                               List<String> information,
                               List<String> errors,
                               List<StepExecutionReport> steps,
                               String type,
                               String targetName,
                               String targetUrl,
                               String strategy,
                               Map<String, Object> evaluatedInputs,
                               Map<String, Object> stepResults,
                               Map<String, Object> scenarioContext
    ) {
        this.executionId = executionId;
        this.name = name;
        this.environment = environment;
        this.duration = duration;
        this.startDate = startDate;
        this.status = status;
        this.information = evaluatedInputs != null ? information : emptyList();
        this.errors = evaluatedInputs != null ? errors : emptyList();
        this.steps = steps;
        this.type = type;
        this.targetName = targetName;
        this.targetUrl = targetUrl;
        this.strategy = strategy;
        this.evaluatedInputs = evaluatedInputs != null ? evaluatedInputs : emptyMap();
        this.stepResults = stepResults != null ? stepResults : emptyMap();
        this.scenarioContext = scenarioContext != null ? scenarioContext : emptyMap();
    }

    @Override
    public Status getStatus() {
        return status;
    }
}
