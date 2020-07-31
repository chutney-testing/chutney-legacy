package com.chutneytesting.design.api.scenario.v2_0.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.chutneytesting.execution.api.ExecutionSummaryDto;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableGwtTestCaseDto.class)
@JsonDeserialize(as = ImmutableGwtTestCaseDto.class)
@Value.Style(jdkOnly = true)
public interface GwtTestCaseDto {

    Optional<String> id();

    String title();

    Optional<String> description();

    Optional<String> repositorySource();

    List<String> tags();

    List<ExecutionSummaryDto> executions();

    Optional<Instant> creationDate();

    GwtScenarioDto scenario();

    Map<String, String> computedParameters();
}
