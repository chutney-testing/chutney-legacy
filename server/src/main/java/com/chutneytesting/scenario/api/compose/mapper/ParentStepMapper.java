package com.chutneytesting.scenario.api.compose.mapper;

import static com.chutneytesting.tools.orient.ComposableIdUtils.toFrontId;

import com.chutneytesting.scenario.api.compose.dto.ImmutableNameIdDto;
import com.chutneytesting.scenario.api.compose.dto.ImmutableParentsStepDto;
import com.chutneytesting.scenario.api.compose.dto.ParentsStepDto;
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
                    Collectors.mapping(p -> ImmutableNameIdDto.builder().name(p.name).id(toFrontId(p.id)).build(), Collectors.toList())
                )
            );

        return ImmutableParentsStepDto.builder()
            .addAllParentScenario(Optional.ofNullable(collect.get(Boolean.TRUE)).orElse(Collections.emptyList()))
            .addAllParentSteps(Optional.ofNullable(collect.get(Boolean.FALSE)).orElse(Collections.emptyList()))
            .build();
    }
}
