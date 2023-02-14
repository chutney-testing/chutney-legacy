package com.chutneytesting.scenario.domain.gwt;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import com.chutneytesting.server.core.domain.execution.Strategy;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GwtStep {

    public static GwtStep NONE = new GwtStep("", emptyList(), GwtStepImplementation.NONE, Strategy.NONE, empty());

    public final String description;
    public final List<GwtStep> subSteps;
    public final Optional<GwtStepImplementation> implementation;
    public final Optional<Strategy> strategy;
    public final Optional<String>  xRef;

    private GwtStep(String description, List<GwtStep> subSteps, Optional<GwtStepImplementation> implementation, Optional<Strategy> strategy, Optional<String> xRef) {
        this.description = description;
        this.subSteps = subSteps;
        this.implementation = implementation;
        this.strategy = strategy;
        this.xRef = xRef;
    }

    public static GwtStepBuilder builder() {
        return new GwtStepBuilder();
    }

    @Override
    public String toString() {
        return "GwtStep{" +
            "description='" + description + '\'' +
            ", subSteps=" + subSteps +
            ", implementation=" + implementation.map(GwtStepImplementation::toString).orElse("{}") +
            ", strategy=" + strategy.map(Strategy::toString).orElse("{}") +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GwtStep gwtStep = (GwtStep) o;
        return description.equals(gwtStep.description) &&
            subSteps.equals(gwtStep.subSteps) &&
            implementation.equals(gwtStep.implementation) &&
            strategy.equals(gwtStep.strategy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, subSteps, implementation, strategy);
    }

    public static class GwtStepBuilder {
        private String description ;
        private Optional<String> xRef ;
        private List<GwtStep> subSteps;
        private Optional<GwtStepImplementation> implementation;
        private Optional<Strategy> strategy;

        private GwtStepBuilder() {}

        public GwtStep build() {
            return new GwtStep(
                ofNullable(description).orElse(""),
                ofNullable(subSteps).orElse(emptyList()),
                ofNullable(implementation).orElse(GwtStepImplementation.NONE),
                ofNullable(strategy).orElse(Strategy.NONE),
                ofNullable(xRef).orElse(Optional.empty()));
        }

        public GwtStepBuilder withXRef(String xRef) {
            this.xRef = ofNullable(xRef);
            return this;
        }

        public GwtStepBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public GwtStepBuilder withSubSteps(GwtStep... subSteps) {
            this.subSteps = Arrays.asList(subSteps);
            return this;
        }

        public GwtStepBuilder withSubSteps(List<GwtStep> subSteps) {
            this.subSteps = subSteps;
            return this;
        }

        public GwtStepBuilder withImplementation(GwtStepImplementation implementation) {
            this.implementation = ofNullable(implementation);
            return this;
        }

        public GwtStepBuilder withStrategy(Strategy strategy) {
            this.strategy = ofNullable(strategy);
            return this;
        }
    }

}

