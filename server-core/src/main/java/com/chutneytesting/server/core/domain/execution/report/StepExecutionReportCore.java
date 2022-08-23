package com.chutneytesting.server.core.domain.execution.report;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class StepExecutionReportCore implements ServerReportStatus.HavingStatus {

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
    public StepExecutionReportCore(String name,
                                   Long duration,
                                   Instant startDate,
                                   ServerReportStatus status,
                                   List<String> information,
                                   List<String> errors,
                                   List<StepExecutionReportCore> steps,
                                   String type,
                                   String targetName,
                                   String targetUrl,
                                   String strategy
    ) {
        this(name, duration, startDate, status, information, errors, steps, type, targetName, targetUrl, strategy, null, null);
    }

    public StepExecutionReportCore(String name,
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
