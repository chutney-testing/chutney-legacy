/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.agent.api.mapper;

import com.chutneytesting.agent.api.dto.ExploreResultApiDto;
import com.chutneytesting.agent.api.dto.ExploreResultApiDto.AgentLinkEntity;
import com.chutneytesting.agent.api.dto.ExploreResultApiDto.TargetLinkEntity;
import com.chutneytesting.agent.api.dto.TargetIdEntity;
import com.chutneytesting.agent.domain.TargetId;
import com.chutneytesting.agent.domain.explore.AgentId;
import com.chutneytesting.agent.domain.explore.ExploreResult;
import com.chutneytesting.agent.domain.explore.ImmutableExploreResult;
import com.chutneytesting.agent.domain.explore.ImmutableExploreResult.Link;
import com.chutneytesting.agent.domain.explore.ImmutableExploreResult.Links;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ExploreResultApiMapper {

    public ExploreResult fromDto(ExploreResultApiDto linksEntity, AgentLinkEntity linkToExploredAgent) {
        return ImmutableExploreResult.builder()
            .agentLinks(
                Links.<AgentId, AgentId>builder()
                    .addAllLinks(
                        linksEntity.agentLinks.stream()
                            .map(linkEntity -> Link.of(AgentId.of(linkEntity.source), AgentId.of(linkEntity.destination)))
                            .collect(Collectors.toSet())
                    ).addLinks(Link.of(AgentId.of(linkToExploredAgent.source), AgentId.of(linkToExploredAgent.destination)))
                    .build()
            )
            .targetLinks(
                Links.of(
                    linksEntity.targetLinks.stream()
                        .map(targetLinkEntity -> Link.of(AgentId.of(targetLinkEntity.source), from(targetLinkEntity)))
                        .collect(Collectors.toSet())
                )
            )
            .build();
    }

    private TargetId from(TargetLinkEntity targetLinkEntity) {
        return TargetId.of(targetLinkEntity.destination.name, targetLinkEntity.destination.environment);
    }

    public ExploreResultApiDto from(ExploreResult exploreResult) {
        ExploreResultApiDto dto = new ExploreResultApiDto();

        dto.agentLinks = exploreResult.agentLinks().stream()
            .map(link -> new AgentLinkEntity(link.source().name(), link.destination().name()))
            .collect(Collectors.toSet());

        dto.targetLinks.addAll(
            exploreResult.targetLinks().stream()
                .map(link -> new TargetLinkEntity(link.source().name(), from(link.destination())))
                .collect(Collectors.toSet())
        );

        return dto;
    }

    private TargetIdEntity from(TargetId destination) {
        return new TargetIdEntity(destination.name, destination.environment);
    }
}
