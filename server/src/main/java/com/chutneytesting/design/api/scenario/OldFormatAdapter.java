package com.chutneytesting.design.api.scenario;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;
import static com.chutneytesting.design.api.scenario.OldFormatAdapter.ScenarioV1.StepV1.GwtType.WHEN;
import static com.chutneytesting.design.api.scenario.v2_0.mapper.GwtScenarioMapper.mapper;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.chutneytesting.design.domain.scenario.ScenarioNotParsableException;
import com.chutneytesting.design.domain.scenario.gwt.GwtScenario;
import com.chutneytesting.design.domain.scenario.gwt.GwtStep;
import com.chutneytesting.design.domain.scenario.gwt.GwtStepImplementation;
import com.chutneytesting.design.domain.scenario.gwt.Strategy;
import com.chutneytesting.execution.domain.compiler.ScenarioConversionException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hjson.JsonValue;
import com.chutneytesting.tools.Streams;

public class OldFormatAdapter {

    public static GwtScenario from(String title, String description, String jsonScenario) {
        return fromV0(jsonScenario).toGwt(title, description);
    }

    static Convertible fromV0(String jsonScenario) {
        try {
            return mapper.readValue(jsonScenario, ScenarioV0.class);
        } catch (IOException e) {
            return fromV1(jsonScenario);
        }
    }

    static Convertible fromV1(String jsonScenario) {
        try {
            return mapper.readValue(jsonScenario, ScenarioV1.class);
        } catch (IOException e) {
            return fromV2_0(jsonScenario);
        }
    }

    static Convertible fromV2_0(String jsonScenario) {
        try {
            return mapper.readValue(jsonScenario, ScenarioV2_0.class);
        } catch (IOException e) {
            throw new ScenarioNotParsableException("Cannot deserialize scenario: ", e);
        }
    }

    interface Convertible {
        GwtScenario toGwt(String title, String description);
    }

    static class ScenarioV0 implements Convertible {
        @JsonProperty("scenario")
        StepV0 rootStep = StepV0.NONE;

        public GwtScenario toGwt(String title, String description) {
            return GwtScenario.builder()
                .withTitle(title)
                .withDescription(description)
                .withGivens(rootStep.steps.stream().map(StepV0::toGwt).collect(Collectors.toList()))
                .withWhen(GwtStep.NONE)
                .withThens(Collections.singletonList(GwtStep.NONE))
                .build();
        }

        static class StepV0 {
            static StepV0 NONE = new StepV0();

            @JsonProperty(access = WRITE_ONLY)
            String name = "";
            @JsonProperty(access = WRITE_ONLY)
            List<StepV0> steps = emptyList();
            @JsonProperty(access = WRITE_ONLY)
            StepStrategyV0 strategy = StepStrategyV0.NONE;

            // Task
            public String type;
            public String target;
            public Map<String, Object> inputs;
            public Map<String, Object> outputs;
            public Map<String, Object> validations;

            GwtStep toGwt() {
                GwtStep.GwtStepBuilder builder = GwtStep.builder()
                    .withDescription(name)
                    .withSubSteps(
                        steps.stream().map(StepV0::toGwt).collect(Collectors.toList())
                    );

                if (type != null && !type.isEmpty()) {
                    builder.withImplementation(new GwtStepImplementation(type, target, inputs, outputs, validations, ""));
                }

                if (!strategy.equals(StepStrategyV0.NONE)) {
                    builder.withStrategy(new Strategy(strategy.type, strategy.parameters));
                }

                return builder.build();
            }
        }

        static class StepStrategyV0 {
            static StepStrategyV0 NONE = new StepStrategyV0();

            public String type = "";
            public Map<String, Object> parameters = Collections.emptyMap();
        }

    }

    static class ScenarioV1 implements Convertible {
        @JsonProperty("scenario")
        StepV1 rootStep = StepV1.NONE;

        public GwtScenario toGwt(String title, String description) {
            StepV1 lastWhen = Streams.findLast(rootStep.steps.stream(), s -> s.gwtType != null && s.gwtType.equals(WHEN)).orElse(StepV1.NONE);

            return GwtScenario.builder()
                .withTitle(title)
                .withDescription(description)
                .withGivens(Streams.takeUntil(rootStep.steps.stream(), s -> s.equals(lastWhen))
                    .map(StepV1::toGwt)
                    .collect(Collectors.toList())
                )
                .withWhen(Optional.of(lastWhen).map(StepV1::toGwt).orElse(GwtStep.NONE))
                .withThens(Streams.skipUntil(rootStep.steps.stream(), s -> s.equals(lastWhen))
                    .map(StepV1::toGwt)
                    .collect(Collectors.toList())
                )
                .build();
        }

        static class StepV1 {

            enum GwtType {
                ROOT_STEP,
                GIVEN, WHEN, THEN
            }

            static StepV1 NONE = new StepV1();

            @JsonProperty(access = WRITE_ONLY, required = true)
            GwtType gwtType;
            @JsonProperty(access = WRITE_ONLY)
            String name = "";
            @JsonProperty(access = WRITE_ONLY)
            List<ScenarioV1.StepV1> steps = emptyList();
            @JsonProperty(access = WRITE_ONLY)
            StepStrategyV1 strategy = StepStrategyV1.NONE;

            // Task
            public String type;
            public String target;
            public Map<String, Object> inputs;
            public Map<String, Object> outputs;
            public Map<String, Object> validations;

            GwtStep toGwt() {
                GwtStep.GwtStepBuilder builder = GwtStep.builder()
                    .withDescription(name)
                    .withSubSteps(
                        steps.stream().map(StepV1::toGwt).collect(Collectors.toList())
                    );

                if (type != null && !type.isEmpty()) {
                    builder.withImplementation(new GwtStepImplementation(type, target, inputs, outputs, validations, ""));
                }

                if (!strategy.equals(StepStrategyV1.NONE)) {
                    builder.withStrategy(new Strategy(strategy.type, strategy.parameters));
                }

                return builder.build();
            }
        }

        static class StepStrategyV1 {
            static StepStrategyV1 NONE = new StepStrategyV1();

            public String type = "";
            public Map<String, Object> parameters = Collections.emptyMap();
        }
    }

    static class ScenarioV2_0 implements Convertible {
        @JsonProperty(access = WRITE_ONLY)
        String title = "";
        @JsonProperty(access = WRITE_ONLY)
        String description = "";
        @JsonProperty(access = WRITE_ONLY)
        List<GwtStep2_0> givens = emptyList();
        @JsonProperty(access = WRITE_ONLY)
        GwtStep2_0 when;
        @JsonProperty(access = WRITE_ONLY)
        List<GwtStep2_0> thens = emptyList();

        static class GwtStep2_0 {
            @JsonProperty(access = WRITE_ONLY)
            String description = "";
            @JsonProperty(access = WRITE_ONLY)
            List<GwtStep2_0> subSteps = emptyList();
            @JsonProperty(access = WRITE_ONLY)
            Optional<GwtStepImplementationV2_0> implementation = empty();
            @JsonProperty(access = WRITE_ONLY)
            Optional<Strategy> strategy = empty();

            GwtStep toGwt() {
                GwtStep.GwtStepBuilder builder = GwtStep.builder()
                    .withDescription(description)
                    .withSubSteps(subSteps.stream().map(GwtStep2_0::toGwt).collect(Collectors.toList()));
                strategy.ifPresent(builder::withStrategy);
                implementation.ifPresent(i -> builder.withImplementation(i.toGwt()));
                return builder.build();
            }
        }

        static class GwtStepImplementationV2_0 {
            @JsonProperty(access = WRITE_ONLY)
            String task = "";

            GwtStepImplementation toGwt() {
                try {
                    return mapper.readValue(JsonValue.readHjson(task).toString(), GwtStepImplementation.class);
                } catch (IOException e) {
                    throw new ScenarioConversionException(e);
                }
            }
        }

        public GwtScenario toGwt(String title, String description) {
            return GwtScenario.builder()
                .withTitle(title)
                .withDescription(description)
                .withGivens(givens.stream().map(GwtStep2_0::toGwt).collect(Collectors.toList()))
                .withWhen(when.toGwt())
                .withThens(thens.stream().map(GwtStep2_0::toGwt).collect(Collectors.toList()))
                .build();
        }
    }
}
