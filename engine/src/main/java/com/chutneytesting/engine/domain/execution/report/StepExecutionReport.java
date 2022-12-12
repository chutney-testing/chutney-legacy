package com.chutneytesting.engine.domain.execution.report;

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
    public final Map<String, Object> inputs;

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
        this(executionId, name, environment, duration, startDate, status, information, errors, steps, type, targetName, targetUrl, strategy, null, null, null, null);
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
                               Map<String, Object> inputs,
                               Map<String, Object> stepResults,
                               Map<String, Object> scenarioContext
    ) {
        this.executionId = executionId;
        this.name = name;
        this.environment = environment;
        this.duration = duration;
        this.startDate = startDate;
        this.status = status;
        this.information = information;
        this.errors = errors;
        this.steps = steps;
        this.type = type;
        this.targetName = targetName;
        this.targetUrl = targetUrl;
        this.strategy = strategy;
        this.evaluatedInputs = evaluatedInputs != null ? evaluatedInputs : emptyMap();
        this.inputs = inputs != null ? inputs : emptyMap();
        this.stepResults = stepResults != null ? stepResults : emptyMap();
        this.scenarioContext = scenarioContext != null ? scenarioContext : emptyMap();
    }

    @Override
    public Status getStatus() {
        return status;
    }
}
