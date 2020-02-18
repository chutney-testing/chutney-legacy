package com.chutneytesting.design.domain.scenario.gwt;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GwtScenario {

    public final String title;
    public final String description;
    public final List<GwtStep> givens;
    public final GwtStep when;
    public final List<GwtStep> thens;

    private GwtScenario(String title, String description, List<GwtStep> givens, GwtStep when, List<GwtStep> thens) {
        this.title = title;
        this.description = description;
        this.givens = givens;
        this.when = when;
        this.thens = thens;
    }

    public List<GwtStep> steps() {
        List<GwtStep> all = new ArrayList<>(givens);
        all.add(when);
        all.addAll(thens);
        return unmodifiableList(all);
    }

    @Override
    public String toString() {
        return "GwtScenario{" +
            "givens=" + givens +
            ", when=" + when +
            ", thens=" + thens +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GwtScenario that = (GwtScenario) o;
        return givens.equals(that.givens) &&
            when.equals(that.when) &&
            thens.equals(that.thens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(givens, when, thens);
    }

    public static GwtScenarioBuilder builder() {
        return new GwtScenarioBuilder();
    }

    public static class GwtScenarioBuilder {

        private String title;
        private String description;
        private List<GwtStep> givens;
        private GwtStep when;
        private List<GwtStep> thens;

        private GwtScenarioBuilder() {}

        public GwtScenario build() {
            return new GwtScenario(
                ofNullable(title).orElse(""),
                ofNullable(description).orElse(""),
                ofNullable(givens).orElse(emptyList()),
                ofNullable(when).orElseThrow(IllegalStateException::new),
                ofNullable(thens).orElse(emptyList())
            );
        }

        public GwtScenarioBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public GwtScenarioBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public GwtScenarioBuilder withGivens(List<GwtStep> givens) {
            this.givens = unmodifiableList(givens);
            return this;
        }

        public GwtScenarioBuilder withWhen(GwtStep when) {
            this.when = when;
            return this;
        }

        public GwtScenarioBuilder withThens(List<GwtStep> thens) {
            this.thens = unmodifiableList(thens);
            return this;
        }

    }

}
