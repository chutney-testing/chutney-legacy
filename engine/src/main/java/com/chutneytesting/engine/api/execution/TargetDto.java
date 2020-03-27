package com.chutneytesting.engine.api.execution;

import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TargetDto {

    public final String id;
    public final String url;
    public final Map<String, String> properties;
    public final SecurityInfoDto security;
    public final String name;
    public final List<NamedHostAndPort> agents;

    public TargetDto(String id, String url, Map<String, String> properties, SecurityInfoDto security, List<NamedHostAndPort> agents) {
        this.id = id;
        this.name = id;
        this.url = url;
        this.properties = properties;
        this.security = security;
        this.agents = agents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetDto targetDto = (TargetDto) o;
        return id.equals(targetDto.id) &&
            url.equals(targetDto.url) &&
            properties.equals(targetDto.properties) &&
            security.equals(targetDto.security) &&
            agents.equals(targetDto.agents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, properties, security, agents);
    }

    @Override
    public String toString() {
        return "TargetDto{" +
            "id='" + id + '\'' +
            ", url='" + url + '\'' +
            ", properties=" + properties +
            ", security=" + security +
            ", agents=" + agents +
            '}';
    }

}
