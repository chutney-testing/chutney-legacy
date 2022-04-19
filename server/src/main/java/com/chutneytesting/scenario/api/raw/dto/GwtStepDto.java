package com.chutneytesting.scenario.api.raw.dto;

import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableGwtStepDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableGwtStepDto.class)
@JsonDeserialize(as = ImmutableGwtStepDto.class)
@Value.Style(jdkOnly = true)
public interface GwtStepDto {

    Optional<String> sentence();

    List<GwtStepDto> subSteps();

    Optional<GwtStepImplementationDto> implementation();

    Optional<StrategyDto> strategy();

    @JsonProperty("x-$ref") Optional<String> xRef();

}

