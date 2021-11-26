package com.chutneytesting.jira.api;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.time.Instant;
import java.util.List;

public class ReportForJira {

    public final Instant startDate;
    public final Long duration;
    public final String status;
    public final Step rootStep;
    public final String environment;

    public ReportForJira(Instant startDate, Long duration, String status, Step rootStep, String environment) {
        this.startDate = startDate;
        this.duration = duration;
        this.status = status;
        this.rootStep = rootStep;
        this.environment = environment;
    }

    public static class Step {
        public final String name;
        public final List<String> errors;
        public final List<Step> steps;

        public Step(String name, List<String> errors, List<Step> steps) {
            this.name = name;
            this.errors = ofNullable(errors).orElse(emptyList());
            this.steps = ofNullable(steps).orElse(emptyList());
        }
    }
}
