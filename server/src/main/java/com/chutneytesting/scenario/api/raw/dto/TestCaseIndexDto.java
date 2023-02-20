package com.chutneytesting.scenario.api.raw.dto;

import static java.util.Collections.emptyList;

import com.chutneytesting.execution.api.ExecutionSummaryDto;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableTestCaseIndexDto.class)
@JsonDeserialize(as = ImmutableTestCaseIndexDto.class)
public interface TestCaseIndexDto {

    GwtTestCaseMetadataDto metadata();

    static TestCaseIndexDto from(TestCaseMetadata testCaseMetadata) {
        return ImmutableTestCaseIndexDto.builder()
            .metadata(ImmutableGwtTestCaseMetadataDto.builder()
                .id(testCaseMetadata.id())
                .creationDate(testCaseMetadata.creationDate())
                .updateDate(testCaseMetadata.updateDate())
                .title(testCaseMetadata.title())
                .description(testCaseMetadata.description())
                .tags(testCaseMetadata.tags())
                .executions(emptyList())
                .build()
            )
            .build();
    }
    static TestCaseIndexDto from(TestCaseMetadata testCaseMetadata, ExecutionSummaryDto execution) {
        return ImmutableTestCaseIndexDto.builder()
            .metadata(ImmutableGwtTestCaseMetadataDto.builder()
                .id(testCaseMetadata.id())
                .creationDate(testCaseMetadata.creationDate())
                .updateDate(testCaseMetadata.updateDate())
                .title(testCaseMetadata.title())
                .description(testCaseMetadata.description())
                .tags(testCaseMetadata.tags())
                .executions(List.of(execution))
                .build()
            )
            .build();
    }
}
