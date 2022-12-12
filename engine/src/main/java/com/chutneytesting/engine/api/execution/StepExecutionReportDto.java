package com.chutneytesting.engine.api.execution;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class StepExecutionReportDto {

    public String name;
    public String environment;
    public long duration;
    public Instant startDate;
    public StatusDto status;
    public List<String> information;
    public List<String> errors;
    public List<StepExecutionReportDto> steps;
    public StepContextDto context;
    public String type;
    public String targetName;
    public String targetUrl;
    public String strategy;

    public StepExecutionReportDto() {
    }

    public StepExecutionReportDto(String name,
                                  String environment,
                                  Instant startDate,
                                  long duration,
                                  StatusDto status,
                                  List<String> information,
                                  List<String> errors,
                                  List<StepExecutionReportDto> steps,
                                  StepContextDto context,
                                  String type,
                                  String targetName,
                                  String targetUrl,
                                  String strategy
    ) {
        this.name = name;
        this.environment = environment;
        this.startDate = startDate;
        this.duration = duration;
        this.status = status;
        this.information = information;
        this.errors = errors;
        this.steps = steps;
        this.context = context;
        this.type = type;
        this.targetName = targetName;
        this.targetUrl = targetUrl;
        this.strategy = strategy;
    }

    public static class StepContextDto {

        public Map<String, Object> scenarioContext;
        public Map<String, Object> evaluatedInputs;
        public Map<String, Object> inputs;
        public Map<String, Object> stepResults;

        public StepContextDto() {
        }

        public StepContextDto(Map<String, Object> scenarioContext, Map<String, Object> evaluatedInputs, Map<String, Object> inputs,  Map<String, Object> stepResults) {
            this.scenarioContext = scenarioContext;
            this.evaluatedInputs = evaluatedInputs;
            this.inputs = inputs;
            this.stepResults = stepResults;
        }
    }
}
