package com.chutneytesting.execution.domain.report;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class StepExecutionReportCore implements ServerReportStatus.HavingStatus {

    public final Long executionId;
    public final String name;
    public final Long duration;
    public final Instant startDate;
    public final ServerReportStatus status;
    public final List<String> information;
    public final List<String> errors;
    public final List<StepExecutionReportCore> steps;
    public final String type;
    public final String targetName;
    public final String targetUrl;
    public final String strategy;
    public final Map<String, Object> evaluatedInputs;
    public final Map<String, Object> stepOutputs;

    @JsonCreator
    public StepExecutionReportCore(@JsonProperty("executionId") Long executionId,
                                   @JsonProperty("name") String name,
                                   @JsonProperty("duration") Long duration,
                                   @JsonProperty("startDate") Instant startDate,
                                   @JsonProperty("status") ServerReportStatus status,
                                   @JsonProperty("information") List<String> information,
                                   @JsonProperty("errors") List<String> errors,
                                   @JsonProperty("steps") List<StepExecutionReportCore> steps,
                                   @JsonProperty("type") String type,
                                   @JsonProperty("targetName") String targetName,
                                   @JsonProperty("targetUrl") String targetUrl,
                                   @JsonProperty("strategy") String strategy
    ) {
        this(executionId, name, duration, startDate, status, information, errors, steps, type, targetName, targetUrl, strategy, null, null);
    }

    public StepExecutionReportCore(Long executionId,
                                   String name,
                                   Long duration,
                                   Instant startDate,
                                   ServerReportStatus status,
                                   List<String> information,
                                   List<String> errors,
                                   List<StepExecutionReportCore> steps,
                                   String type,
                                   String targetName,
                                   String targetUrl,
                                   String strategy,
                                   Map<String, Object> evaluatedInputs,
                                   Map<String, Object> stepOutputs
    ) {
        this.executionId = executionId;
        this.name = name;
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
        this.evaluatedInputs = evaluatedInputs;
        this.stepOutputs = stepOutputs;
    }

    @Override
    public ServerReportStatus getStatus() {
        return status;
    }

    @JsonIgnore
    public boolean isTerminated() {
        return status.isFinal();
    }
}
