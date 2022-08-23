package com.chutneytesting.server.core.domain.execution.report;

import static java.util.Collections.emptyList;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StepExecutionReportCoreBuilder {

    private String name;
    private long duration;
    private Instant startDate;
    private ServerReportStatus status;
    private List<String> information;
    private List<String> errors;
    private List<StepExecutionReportCore> steps;
    private String type;
    private String targetName = "";
    private String targetUrl = "";
    private String strategy = "sequential";
    private Map<String, Object> evaluatedInputs;
    private Map<String, Object> stepOutputs;

    public StepExecutionReportCoreBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public StepExecutionReportCoreBuilder setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public StepExecutionReportCoreBuilder setStartDate(Instant startDate) {
        this.startDate = startDate;
        return this;
    }

    public StepExecutionReportCoreBuilder setStatus(ServerReportStatus status) {
        this.status = status;
        return this;
    }

    public StepExecutionReportCoreBuilder setInformation(List<String> information) {
        this.information = information;
        return this;
    }

    public StepExecutionReportCoreBuilder setErrors(List<String> errors) {
        this.errors = errors;
        return this;
    }

    public StepExecutionReportCoreBuilder setSteps(List<StepExecutionReportCore> steps) {
        this.steps = steps;
        return this;
    }

    public StepExecutionReportCoreBuilder setType(String type) {
        this.type = type;
        return this;
    }

    public StepExecutionReportCoreBuilder setTargetName(String name) {
        this.targetName = name;
        return this;
    }

    public StepExecutionReportCoreBuilder setTargetUrl(String url) {
        this.targetUrl = url;
        return this;
    }

    public StepExecutionReportCoreBuilder setEvaluatedInputs(Map<String, Object> evaluatedInputs) {
        this.evaluatedInputs = evaluatedInputs;
        return this;
    }

    public StepExecutionReportCoreBuilder setStepOutputs(Map<String, Object> stepOutputs) {
        this.stepOutputs = stepOutputs;
        return this;
    }

    public StepExecutionReportCoreBuilder setStrategy(String strategy) {
        if (strategy != null) {
            this.strategy = strategy;
        }

        return this;
    }

    public StepExecutionReportCore createStepExecutionReport() {
        return new StepExecutionReportCore(
            name,
            duration,
            startDate,
            status,
            Optional.ofNullable(information).orElse(emptyList()),
            Optional.ofNullable(errors).orElse(emptyList()),
            Optional.ofNullable(steps).orElse(emptyList()),
            type,
            targetName,
            targetUrl,
            strategy,
            evaluatedInputs,
            stepOutputs
        );
    }


}
