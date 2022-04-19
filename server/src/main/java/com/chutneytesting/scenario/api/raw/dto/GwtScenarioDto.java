package com.chutneytesting.scenario.api.raw.dto;

import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableGwtScenarioDto;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableGwtScenarioDto.class)
@JsonDeserialize(as = ImmutableGwtScenarioDto.class)
@Value.Style(jdkOnly = true)
public interface GwtScenarioDto {

    List<GwtStepDto> givens();

    GwtStepDto when();

    List<GwtStepDto> thens();

}
