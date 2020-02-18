package com.chutneytesting.agent.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.chutneytesting.agent.domain.configure.NetworkConfiguration;
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
        public Set<TargetsApiDto> targetsConfiguration = new HashSet<>();

        public EnvironmentApiDto(@JsonProperty("name") String name,
                                 @JsonProperty("targets") Set<TargetsApiDto> targetsConfiguration) {
            this.name = name;
            this.targetsConfiguration = targetsConfiguration;
        }
    }

    public static class TargetsApiDto {
        public final String name;
        public final String url;
        public final Map<String, String> properties;
        public final SecurityApiDto security;

        public TargetsApiDto(
            @JsonProperty("name") String name,
            @JsonProperty("url") String url,
            @JsonProperty("properties") Map<String, String> properties,
            @JsonProperty("security") SecurityApiDto security) {
            this.name = name;
            this.url = url;
            this.properties = properties != null ? properties : new HashMap<>();
            this.security = security != null ? security : new SecurityApiDto(null, null, null, null, null, null);
        }
    }

    public static class SecurityApiDto {
        public final String username;
        public final String password;
        public final String keyStore;
        public final String keyStorePassword;
        public final String trustStore;
        public final String trustStorePassword;

        public SecurityApiDto(
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("keyStore") String keyStore,
            @JsonProperty("keyStorePassword") String keyStorePassword,
            @JsonProperty("trustStore") String trustStore,
            @JsonProperty("trustStorePassword") String trustStorePassword) {
            this.username = username;
            this.password = password;
            this.keyStore = keyStore;
            this.keyStorePassword = keyStorePassword;
            this.trustStore = trustStore;
            this.trustStorePassword = trustStorePassword;
        }
    }
}
