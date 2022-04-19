package com.chutneytesting.design.api.scenario.v2_0.mapper;

import static com.chutneytesting.design.domain.scenario.gwt.GwtStep.NONE;
import static com.fasterxml.jackson.annotation.PropertyAccessor.CREATOR;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.annotation.PropertyAccessor.GETTER;
import static com.fasterxml.jackson.annotation.PropertyAccessor.SETTER;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.INDENT_ARRAYS;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.SPLIT_LINES;
import static org.hjson.JsonValue.readHjson;

import com.chutneytesting.design.api.scenario.OldFormatAdapter;
import com.chutneytesting.design.api.scenario.v2_0.dto.GwtScenarioDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.GwtStepDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.GwtStepImplementationDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableGwtScenarioDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableGwtStepDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableGwtStepImplementationDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.StrategyDto;
import com.chutneytesting.design.domain.scenario.ScenarioNotParsableException;
import com.chutneytesting.design.domain.scenario.gwt.GwtScenario;
import com.chutneytesting.design.domain.scenario.gwt.GwtStep;
import com.chutneytesting.design.domain.scenario.gwt.GwtStepImplementation;
import com.chutneytesting.design.domain.scenario.gwt.Strategy;
import com.chutneytesting.execution.domain.compiler.GwtScenarioMarshaller;
import com.chutneytesting.execution.domain.compiler.ScenarioConversionException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hjson.Stringify;
import org.springframework.stereotype.Component;

@Component
public class GwtScenarioMapper implements GwtScenarioMarshaller {
    // TODO - Refactor mappers scattered everywhere :)
    public static ObjectMapper mapper = configureMapper(new ObjectMapper());

    public static ObjectMapper yamlMapper = configureMapper(new YAMLMapper()
        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        .enable(MINIMIZE_QUOTES)
        .enable(INDENT_ARRAYS)
        .enable(INDENT_ARRAYS_WITH_INDICATOR)
        .disable(SPLIT_LINES)
    );

    @JsonDeserialize(builder = GwtScenario.GwtScenarioBuilder.class)
    private static class GwtScenarioMixin {
    }

    @JsonDeserialize(builder = GwtStep.GwtStepBuilder.class)
    private static class GwtStepMixin {
    }

    private static class GwtStepBuilderMixin {
        @JsonProperty("x-$ref")
        String xRef;
    }

    private static class GwtStepImplementationMixin {

        @JsonProperty("x-$ref")
        String xRef;

        @JsonCreator
        public GwtStepImplementationMixin(String type,
                                          String target,
                                          @JsonInclude(JsonInclude.Include.ALWAYS) Map<String, Object> inputs, // Do not remove JsonInclude
                                          Map<String, Object> outputs,
                                          Map<String, Object> validations,
                                          String xRef
        ) {
        }
    }

    private static class StrategyMixin {
        @JsonCreator
        public StrategyMixin(String type,
                             Map<String, Object> parameters) {
        }
    }

    // DTO -> Scenario
    public static GwtScenario fromDto(String title, String desc, GwtScenarioDto dto) {
        return GwtScenario.builder()
            .withTitle(title)
            .withDescription(desc)
            .withGivens(dto.givens().stream().map(GwtScenarioMapper::fromDto).collect(Collectors.toList()))
            .withWhen(GwtScenarioMapper.fromDto(dto.when()))
            .withThens(dto.thens().stream().map(GwtScenarioMapper::fromDto).collect(Collectors.toList()))
            .build();
    }

    // DTO -> Step
    private static GwtStep fromDto(GwtStepDto dto) {
        GwtStep.GwtStepBuilder builder = GwtStep.builder();

        dto.sentence().ifPresent(builder::withDescription);
        dto.xRef().ifPresent(builder::withXRef);
        builder.withSubSteps(dto.subSteps().stream().map(GwtScenarioMapper::fromDto).collect(Collectors.toList()));
        dto.implementation().ifPresent(i -> builder.withImplementation(fromDto(i)));
        dto.strategy().ifPresent(s -> builder.withStrategy(new Strategy(s.getType(), s.getParameters())));

        return builder.build();
    }

    // DTO -> Implementation
    private static GwtStepImplementation fromDto(GwtStepImplementationDto dto) {
        if (dto.task().isEmpty()) {
            return new GwtStepImplementation(dto.type(), dto.target(), dto.inputs(), dto.outputs(), dto.validations(), dto.xRef());
        } else {
            try {
                return mapper.readValue(readHjson(dto.task()).toString(), GwtStepImplementation.class);
            } catch (IOException e) {
                throw new ScenarioConversionException(e);
            }
        }
    }

    public static GwtScenarioDto toDto(GwtScenario scenario) {
        return ImmutableGwtScenarioDto.builder()
            .givens(toDto(scenario.givens))
            .when(toDto(scenario.when))
            .thens(toDto(scenario.thens))
            .build();
    }

    private static List<GwtStepDto> toDto(List<GwtStep> givens) {
        return givens.stream().map(GwtScenarioMapper::toDto).collect(Collectors.toList());
    }

    private static GwtStepDto toDto(GwtStep step) {
        ImmutableGwtStepDto.Builder builder = ImmutableGwtStepDto.builder();
        builder.sentence(step.description);
        step.implementation.ifPresent(i -> builder.implementation(toDto(i)));
        step.strategy.ifPresent(s -> builder.strategy(toDto(s)));
        step.xRef.ifPresent(builder::xRef);
        builder.subSteps(toDto(step.subSteps));
        return builder.build();
    }

    private static GwtStepImplementationDto toDto(GwtStepImplementation implementation) {
        try {
            return ImmutableGwtStepImplementationDto.builder()
                .task(readHjson(mapper.writeValueAsString(implementation)).toString(Stringify.HJSON))
                .type(implementation.type)
                .target(implementation.target)
                .xRef(implementation.xRef)
                .inputs(implementation.inputs)
                .outputs(implementation.outputs)
                .build();
        } catch (Exception e) {
            throw new ScenarioNotParsableException("Cannot deserialize task implementation", e);
        }
    }

    private static StrategyDto toDto(Strategy strategy) {
        return new StrategyDto(strategy.type, strategy.parameters);
    }

    @Override
    public String serialize(GwtScenario scenario) {
        try {
            return mapper.writeValueAsString(scenario);
        } catch (JsonProcessingException e) {
            throw new ScenarioNotParsableException("Cannot serialize scenario: " + e.getMessage(), e);
        }
    }

    @Override
    public String serializeToYaml(GwtScenario scenario) {
        try {
            return yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(scenario);
        } catch (JsonProcessingException e) {
            throw new ScenarioNotParsableException("Cannot serialize scenario to yaml: " + e.getMessage(), e);
        }
    }

    @Override
    public GwtScenario deserialize(String title, String description, String jsonScenario) {
        try {
            return mapper.readValue(jsonScenario, GwtScenario.class);
        } catch (IOException e) {
            // gracefully fallback on previous versions
            return OldFormatAdapter.from(title, description, jsonScenario);
        }
    }

    @Override
    public GwtScenario deserializeFromYaml(String title, String description, String yamlBlob) {
        try {
            if (StringUtils.isNotEmpty(yamlBlob)) {
                return yamlMapper.readValue(yamlBlob, GwtScenario.class);
            }
            return GwtScenario.builder().withTitle(title).withDescription(description).withWhen(NONE).build();
        } catch (IOException e) {
            // gracefully fallback on previous versions
            String jsonScenario = formatContentToJson(yamlBlob);
            return OldFormatAdapter.from(title, description, jsonScenario);
        }
    }

    private static ObjectMapper configureMapper(ObjectMapper mapper) {
        return mapper
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
            .findAndRegisterModules()
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .setVisibility(FIELD, JsonAutoDetect.Visibility.ANY)
            .setVisibility(GETTER, JsonAutoDetect.Visibility.NONE)
            .setVisibility(SETTER, JsonAutoDetect.Visibility.NONE)
            .setVisibility(CREATOR, JsonAutoDetect.Visibility.NONE)
            .addMixIn(GwtScenario.class, GwtScenarioMixin.class)
            .addMixIn(GwtStep.class, GwtStepMixin.class)
            .addMixIn(GwtStep.GwtStepBuilder.class, GwtStepBuilderMixin.class)
            .addMixIn(GwtStepImplementation.class, GwtStepImplementationMixin.class)
            .addMixIn(Strategy.class, StrategyMixin.class);
    }

    private static String formatContentToJson(String content) {
        try {
            return readHjson(content).toString();
        } catch (Exception e) {
            throw new ScenarioNotParsableException("Malformed json or hjson format. ", e);
        }
    }
}
