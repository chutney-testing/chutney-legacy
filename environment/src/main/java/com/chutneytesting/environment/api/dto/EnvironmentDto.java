package com.chutneytesting.environment.api.dto;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.chutneytesting.environment.domain.Environment;
import java.util.List;
import java.util.stream.Collectors;

public class EnvironmentDto {

    public String name;
    public String description;
    public List<TargetDto> targets;

    public static EnvironmentDto from(Environment environment) {
        EnvironmentDto environmentMetadataDto = new EnvironmentDto();
        environmentMetadataDto.name = environment.name;
        environmentMetadataDto.description = environment.description;
        environmentMetadataDto.targets = environment.targets.stream().map(TargetDto::from).collect(Collectors.toList());
        return environmentMetadataDto;
    }

    public Environment toEnvironment() {
        return Environment.builder()
            .withName(name)
            .withDescription(description)
            .withTargets(
                ofNullable(targets).orElse(emptyList()).stream().map(t -> t.toTarget(name)).collect(Collectors.toSet())
            )
            .build();
    }
}
