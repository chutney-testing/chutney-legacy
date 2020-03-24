package com.chutneytesting.engine.domain.execution.report;

import static java.util.Collections.emptyList;

import com.chutneytesting.engine.domain.environment.Target;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StepExecutionReportBuilder {
    private long executionId;
    private String name;
    private long duration;
    private Instant startDate;
    private Status status;
    private List<String> information;
    private List<String> errors;
    private List<StepExecutionReport> steps;
    private String type;
    private String targetName = "";
    private String targetUrl = "";
    private String strategy = "sequential";
    private Map<String, Object> evaluatedInputs;
    private Map<String, Object> stepResults;
    private Map<String, Object> scenarioContext;

    public StepExecutionReportBuilder from(StepExecutionReport stepExecutionReport) {
        setExecutionId(stepExecutionReport.executionId);
        setName(stepExecutionReport.name);
        setDuration(stepExecutionReport.duration);
        setStartDate(stepExecutionReport.startDate);
        setStatus(stepExecutionReport.status);
        setInformation(stepExecutionReport.information);
        setErrors(stepExecutionReport.errors);
        setSteps(stepExecutionReport.steps);
        setType(stepExecutionReport.type);
        setTargetName(stepExecutionReport.targetName);
        setTargetUrl(stepExecutionReport.targetUrl);
        setStrategy(stepExecutionReport.strategy);
        setEvaluatedInputs(stepExecutionReport.evaluatedInputs);
        setStepResults(stepExecutionReport.stepResults);
        setScenarioContext(stepExecutionReport.scenarioContext);
        return this;
    }

    public StepExecutionReportBuilder setEvaluatedInputs(Map<String, Object> evaluatedInputs) {
        this.evaluatedInputs = evaluatedInputs;
        return this;
    }

    public StepExecutionReportBuilder setStepResults(Map<String, Object> stepResults) {
        this.stepResults = stepResults;
        return this;
    }

    public StepExecutionReportBuilder setScenarioContext(Map<String, Object> scenarioContext) {
        this.scenarioContext = scenarioContext;
        return this;
    }

    private StepExecutionReportBuilder setExecutionId(long executionId) {
        this.executionId = executionId;
        return this;
    }

    public StepExecutionReportBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public StepExecutionReportBuilder setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public StepExecutionReportBuilder setStartDate(Instant startDate) {
        this.startDate = startDate;
        return this;
    }

    public StepExecutionReportBuilder setStatus(Status status) {
        this.status = status;
        return this;
    }

    public StepExecutionReportBuilder setInformation(List<String> information) {
        this.information = information;
        return this;
    }

    public StepExecutionReportBuilder setErrors(List<String> errors) {
        this.errors = errors;
        return this;
    }

    public StepExecutionReportBuilder setSteps(List<StepExecutionReport> steps) {
        this.steps = steps;
        return this;
    }

    public StepExecutionReportBuilder setType(String type) {
        this.type = type;
        return this;
    }

    public StepExecutionReportBuilder setTarget(Target target) {
        if (target != null) {
            this.targetName = target.name;
            this.targetUrl = target.url;
        }
        return this;
    }

    public StepExecutionReportBuilder setTargetName(String name) {
        this.targetName = name;
        return this;
    }

    public StepExecutionReportBuilder setTargetUrl(String url) {
        this.targetUrl = url;
        return this;
    }

    public StepExecutionReportBuilder setStrategy(String strategy) {
        if (strategy != null) {
            this.strategy = strategy;
        }
        return this;
    }

    public StepExecutionReport createStepExecutionReport() {
        return new StepExecutionReport(
            executionId,
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
            stepResults,
            scenarioContext
        );
    }
}
