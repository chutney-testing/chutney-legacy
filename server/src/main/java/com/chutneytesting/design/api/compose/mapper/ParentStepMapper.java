package com.chutneytesting.design.api.compose.mapper;

import static com.chutneytesting.tools.ui.ComposableIdUtils.toFrontId;

import com.chutneytesting.design.api.compose.dto.ImmutableNameIdDto;
import com.chutneytesting.design.api.compose.dto.ImmutableParentsStepDto;
import com.chutneytesting.design.api.compose.dto.ParentsStepDto;
import com.chutneytesting.design.domain.compose.ParentStepId;
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
