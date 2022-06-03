package com.chutneytesting.agent.api.mapper;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.EnvironmentApiDto;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.TargetsApiDto;
import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.tools.Entry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentApiMapper {

    public EnvironmentDto fromDto(EnvironmentApiDto environmentApiDto) {
        List<TargetDto> targets = environmentApiDto.targetsConfiguration.stream().map(this::fromDto).collect(toList());
        return new EnvironmentDto(environmentApiDto.name, null, targets);
    }

    private TargetDto fromDto(TargetsApiDto targetsApiDto) {
        Map<String, String> properties = new LinkedHashMap<>(targetsApiDto.properties);
        return new TargetDto(targetsApiDto.name, targetsApiDto.url, Entry.toEntrySet(properties));
    }

    public EnvironmentApiDto toDto(EnvironmentDto environment) {
        return new EnvironmentApiDto(environment.name, environment.targets.stream().map(this::toDto).collect(toSet()));
    }

    private TargetsApiDto toDto(TargetDto target) {
        Map<String, String> properties = Entry.toMap(target.properties);
        return new TargetsApiDto(target.name, target.url, properties);
    }
}
