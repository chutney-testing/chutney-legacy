package com.chutneytesting.execution.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.chutneytesting.execution.domain.history.ExecutionHistory.Attached;
import com.chutneytesting.execution.domain.history.ExecutionHistory.ExecutionProperties;
import com.chutneytesting.execution.domain.history.ExecutionHistory.ExecutionSummary;
import com.chutneytesting.execution.domain.history.ImmutableExecutionHistory;
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
            .build();
    }
}
