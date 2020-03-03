package com.chutneytesting.agent.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.agent.api.dto.ExploreResultApiDto;
import com.chutneytesting.agent.api.dto.ExploreResultApiDto.AgentLinkEntity;
import com.chutneytesting.agent.api.dto.ExploreResultApiDto.TargetLinkEntity;
import com.chutneytesting.agent.api.dto.TargetIdEntity;
import com.chutneytesting.agent.domain.explore.AgentId;
import com.chutneytesting.agent.domain.explore.ExploreResult;
import com.chutneytesting.agent.domain.explore.ImmutableExploreResult;
import com.chutneytesting.agent.domain.explore.ImmutableExploreResult.Link;
import com.chutneytesting.design.domain.environment.Target;
import java.util.Arrays;
import org.assertj.core.api.Condition;
import org.junit.Test;

public class ExploreResultApiMapperTest {

    ExploreResultApiMapper exploreResultApiMapper = new ExploreResultApiMapper();

    @Test
    public void fromDto_basic_test() {
        ExploreResult exploreResult = ImmutableExploreResult.of(
            ImmutableExploreResult.Links.of(
                Arrays.asList(
                    Link.of(AgentId.of("A"), AgentId.of("B")),
                    Link.of(AgentId.of("B"), AgentId.of("A")))
            ), ImmutableExploreResult.Links.of(
                Arrays.asList(
                    Link.of(AgentId.of("A"), Target.TargetId.of("s1", "env")),
                    Link.of(AgentId.of("B"), Target.TargetId.of("s2", "env")))
            )
        );

        ExploreResultApiDto dto = exploreResultApiMapper.from(exploreResult);

        assertThat(dto.agentLinks).haveExactly(1, new Condition<>(
            link -> "A".equals(link.source) && "B".equals(link.destination),
            "A->B"));
        assertThat(dto.agentLinks).haveExactly(1, new Condition<>(
            link -> "B".equals(link.source) && "A".equals(link.destination),
            "B->A"));

        assertThat(dto.targetLinks).haveExactly(1, new Condition<>(
            link -> "A".equals(link.source) && "s1".equals(link.destination.name),
            "A -> e1|s1"));

        assertThat(dto.targetLinks).haveExactly(1, new Condition<>(
            link -> "B".equals(link.source) && "s2".equals(link.destination.name),
            "B -> e1|s2"));
    }

    @Test
    public void toDto_basic_test() {
        ExploreResultApiDto dto = new ExploreResultApiDto();
        dto.agentLinks.addAll(Arrays.asList(
            new AgentLinkEntity("A", "B"),
            new AgentLinkEntity("B", "A")));

        dto.targetLinks.addAll(
            Arrays.asList(
                new TargetLinkEntity("A", new TargetIdEntity("s1", "env")),
                new TargetLinkEntity("B", new TargetIdEntity("s1", "env"))
            )
        );

        ExploreResult exploreResult = exploreResultApiMapper.fromDto(dto, new AgentLinkEntity("local", "A"));

        assertThat(exploreResult.agentLinks()).hasSize(3);
        assertThat(exploreResult.agentLinks()).haveExactly(1, new Condition<>(
            link -> "local".equals(link.source().name()) && "A".equals(link.destination().name()),
            "local->A"));
        assertThat(exploreResult.agentLinks()).haveExactly(1, new Condition<>(
            link -> "A".equals(link.source().name()) && "B".equals(link.destination().name()),
            "A->B"));
        assertThat(exploreResult.agentLinks()).haveExactly(1, new Condition<>(
            link -> "B".equals(link.source().name()) && "A".equals(link.destination().name()),
            "B->A"));

        assertThat(exploreResult.targetLinks()).haveExactly(1, new Condition<>(
            link -> "A".equals(link.source().name()) && "s1".equals(link.destination().name),
            "B->A"));
        assertThat(exploreResult.targetLinks()).haveExactly(1, new Condition<>(
            link -> "B".equals(link.source().name()) && "s1".equals(link.destination().name),
            "B->A"));
    }
}
