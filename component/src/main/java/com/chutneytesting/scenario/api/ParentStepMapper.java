package com.chutneytesting.scenario.api;

import com.chutneytesting.scenario.api.dto.ImmutableNameIdDto;
import com.chutneytesting.scenario.api.dto.ImmutableParentsStepDto;
import com.chutneytesting.scenario.api.dto.ParentsStepDto;
import com.chutneytesting.scenario.domain.ParentStepId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParentStepMapper {

    public static ParentsStepDto toDto(List<ParentStepId> parentStepId) {

        Map<Boolean, List<ImmutableNameIdDto>> collect = parentStepId
            .stream()
            .collect(
                Collectors.groupingBy(p -> p.isScenario,
                    Collectors.mapping(p -> ImmutableNameIdDto.builder().name(p.name).id(p.id).build(), Collectors.toList())
                )
            );

        return ImmutableParentsStepDto.builder()
            .addAllParentScenario(Optional.ofNullable(collect.get(Boolean.TRUE)).orElse(Collections.emptyList()))
            .addAllParentSteps(Optional.ofNullable(collect.get(Boolean.FALSE)).orElse(Collections.emptyList()))
            .build();
    }
}
