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

package com.chutneytesting.execution.api;

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.Attached;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.ExecutionProperties;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.ExecutionSummary;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as= ImmutableExecutionSummaryDto.class)
@JsonDeserialize(as= ImmutableExecutionSummaryDto.class)
public interface ExecutionSummaryDto extends ExecutionProperties, Attached {

    static List<ExecutionSummaryDto> toDto(Collection<ExecutionSummary> executionSummaryList) {
        return executionSummaryList.stream().map(ExecutionSummaryDto::toDto).collect(Collectors.toList());
    }

    static ExecutionSummaryDto toDto(ExecutionSummary executionSummary) {
        return ImmutableExecutionSummaryDto.builder()
            .time(executionSummary.time())
            .duration(executionSummary.duration())
            .status(executionSummary.status())
            .info(executionSummary.info())
            .error(executionSummary.error())
            .executionId(executionSummary.executionId())
            .testCaseTitle(executionSummary.testCaseTitle())
            .environment(executionSummary.environment())
            .user(executionSummary.user())
            .campaignReport(executionSummary.campaignReport())
            .build();
    }

    static List<ExecutionSummary> fromDto(Collection<ExecutionSummaryDto> executionSummaryList) {
        return executionSummaryList.stream().map(ExecutionSummaryDto::fromDto).collect(Collectors.toList());
    }

    static ExecutionSummary fromDto(ExecutionSummaryDto dto) {
        return ImmutableExecutionHistory.ExecutionSummary.builder()
            .time(dto.time())
            .duration(dto.duration())
            .status(dto.status())
            .info(dto.info())
            .error(dto.error())
            .executionId(dto.executionId())
            .testCaseTitle(dto.testCaseTitle())
            .environment(dto.environment())
            .user(dto.user())
            .build();
    }
}
