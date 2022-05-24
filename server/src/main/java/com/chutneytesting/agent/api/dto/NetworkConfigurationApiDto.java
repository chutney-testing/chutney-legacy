package com.chutneytesting.agent.api.dto;

import com.chutneytesting.agent.domain.configure.NetworkConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * DTO for {@link NetworkConfiguration} transport.
 */
public class NetworkConfigurationApiDto {

    public Instant creationDate;
    public Set<AgentInfoApiDto> agentNetworkConfiguration = new HashSet<>();
    public Set<EnvironmentApiDto> environmentsConfiguration = new HashSet<>();

    public static class AgentInfoApiDto {
        public String name;
        public String host;
        public int port;
    }

    public static class EnvironmentApiDto {
        public String name;
        @JsonProperty("targets")
        public Set<TargetsApiDto> targetsConfiguration;

        public EnvironmentApiDto(String name,
                                 Set<TargetsApiDto> targetsConfiguration) {
            this.name = name;
            this.targetsConfiguration = targetsConfiguration;
        }
    }

    public static class TargetsApiDto {
        public final String name;
        public final String url;
        public final Map<String, String> properties;

        public TargetsApiDto(
            String name,
            String url,
            Map<String, String> properties) {
            this.name = name;
            this.url = url;
            this.properties = properties != null ? properties : new HashMap<>();
        }
    }
}
