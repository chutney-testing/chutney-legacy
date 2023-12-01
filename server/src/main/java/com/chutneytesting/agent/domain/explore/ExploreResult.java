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

package com.chutneytesting.agent.domain.explore;

import com.chutneytesting.agent.domain.TargetId;
import com.chutneytesting.agent.domain.configure.ConfigurationState;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;
import org.immutables.value.Value;

/**
 * Set of {@link Link} representing known agentLinks during {@link ConfigurationState#EXPLORING} phase.
 */
@Value.Immutable
@Value.Enclosing
public interface ExploreResult {
    ExploreResult EMPTY = ImmutableExploreResult.of(Links.empty(), Links.empty());

    @Value.Parameter
    Links<AgentId, AgentId> agentLinks();

    @Value.Parameter
    Links<AgentId, TargetId> targetLinks();

    @Value.Immutable
    interface Links<SOURCE, DESTINATION>  extends Iterable<Link<SOURCE, DESTINATION>> {

        @Value.Parameter
        Set<Link<SOURCE, DESTINATION>> links();

        default Iterator<Link<SOURCE, DESTINATION>> iterator() {
            return links().iterator();
        }

        default Stream<Link<SOURCE, DESTINATION>> stream() {
            return links().stream();
        }

        static <S, D> Links<S, D> empty() {
            return ImmutableExploreResult.Links.<S, D>builder().build();
        }
    }
    /**
     * A directed link, meaning {@link #source()} can reach {@link #destination()}.
     */
    @Value.Immutable
    interface Link<SOURCE, DESTINATION> {
        @Value.Parameter
        SOURCE source();

        @Value.Parameter
        DESTINATION destination();
    }
}
