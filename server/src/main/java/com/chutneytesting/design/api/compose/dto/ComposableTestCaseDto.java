package com.chutneytesting.design.api.compose.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableComposableTestCaseDto.class)
@JsonDeserialize(as = ImmutableComposableTestCaseDto.class)
@Value.Style(jdkOnly = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface ComposableTestCaseDto {

    Optional<String> id();

    String title();

    Optional<String> description();

    @Value.Default()
    default Instant creationDate() {
        return Instant.now();
    }

    List<String> tags();

    ComposableScenarioDto scenario();

    Optional<String> datasetId();

    @Value.Default()
    default List<KeyValue> computedParameters() { return Collections.emptyList(); }

    @Value.Immutable
    @JsonSerialize(as = ImmutableComposableScenarioDto.class)
    @JsonDeserialize(as = ImmutableComposableScenarioDto.class)
    @Value.Style(jdkOnly = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    interface ComposableScenarioDto {

        @Value.Default()
        default List<KeyValue> parameters() { return Collections.emptyList(); }

        @Value.Default
        default List<FunctionalStepDto> componentSteps() { return Collections.emptyList(); }
    }
}
