package com.chutneytesting.agent.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO for ExploreResult transport.
 */
public class ExploreResultApiDto {

    public Set<AgentLinkEntity> agentLinks = new LinkedHashSet<>();

    public final Set<TargetLinkEntity> targetLinks = new LinkedHashSet<>();

    public static class AgentLinkEntity {
        public String source;
        public String destination;

        public AgentLinkEntity() {
        }

        public AgentLinkEntity(String source, String destination) {
            this.source = source;
            this.destination = destination;
        }
    }

    public static class TargetLinkEntity {
        public final String source;
        public final TargetIdEntity destination;

        public TargetLinkEntity(@JsonProperty("source") String source, @JsonProperty("destination") TargetIdEntity destination) {
            this.source = source;
            this.destination = destination;
        }
    }
}
