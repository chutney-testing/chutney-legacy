package com.chutneytesting.agent.api.mapper;

import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.EnvironmentApiDto;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.TargetsApiDto;
import com.chutneytesting.environment.domain.Environment;
import com.chutneytesting.environment.domain.Target;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentApiMapper {

    public Environment fromDto(EnvironmentApiDto environmentApiDto) {
        return Environment.builder()
            .withName(environmentApiDto.name)
            .withTargets(environmentApiDto.targetsConfiguration.stream().map(t -> fromDto(t, environmentApiDto.name)).collect(Collectors.toSet()))
            .build();
    }

    private Target fromDto(TargetsApiDto targetsApiDto, String env) {
        Map<String, String> properties = new LinkedHashMap<>(targetsApiDto.properties);

        return Target.builder()
            .withName(targetsApiDto.name)
            .withEnvironment(env)
            .withUrl(targetsApiDto.url)
            .withProperties(properties)
            .build();
    }

    public EnvironmentApiDto toDto(Environment environment) {
        return new EnvironmentApiDto(environment.name, environment.targets.stream().map(t -> toDto(t)).collect(Collectors.toSet()));
    }

    private TargetsApiDto toDto(Target target) {
        Map<String, String> properties = new LinkedHashMap<>(target.properties);
        return new TargetsApiDto(target.name, target.url, properties);
    }
}
