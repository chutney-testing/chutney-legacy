package com.chutneytesting.design.api.scenario.compose.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableFunctionalStepDto.class)
@JsonDeserialize(as = ImmutableFunctionalStepDto.class)
@Value.Style(jdkOnly = true)
public interface FunctionalStepDto {

    Comparator<FunctionalStepDto> stepDtoComparator = Comparator
        .comparing(FunctionalStepDto::name, String.CASE_INSENSITIVE_ORDER);

    Optional<String> id();

    String name();

    @Value.Default()
    default Strategy strategy() { return ImmutableStrategy.builder().build(); }

    @Value.Default()
    default StepUsage usage() { return StepUsage.STEP; }

    Optional<String> task();

    @Value.Default()
    default List<FunctionalStepDto> steps() { return Collections.emptyList(); }

    @Value.Default()
    default List<KeyValue> parameters() { return Collections.emptyList(); }

    @Value.Default()
    default List<KeyValue> computedParameters() { return Collections.emptyList(); }

    enum StepUsage { STEP, GIVEN, WHEN, THEN }

    @Value.Default()
    default List<String> tags() { return Collections.emptyList(); }
}
