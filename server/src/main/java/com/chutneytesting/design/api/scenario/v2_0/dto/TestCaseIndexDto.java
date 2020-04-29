package com.chutneytesting.design.api.scenario.v2_0.dto;

import static com.chutneytesting.tools.ui.OrientUtils.toFrontId;

import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.execution.api.ExecutionSummaryDto;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableTestCaseIndexDto.class)
@JsonDeserialize(as = ImmutableTestCaseIndexDto.class)
public interface TestCaseIndexDto {

    GwtTestCaseMetadataDto metadata();

    static TestCaseIndexDto from(TestCaseMetadata testCaseMetadata, List<ExecutionSummaryDto> executions) {
        return ImmutableTestCaseIndexDto.builder()
            .metadata(ImmutableGwtTestCaseMetadataDto.builder()
                .id(toFrontId(testCaseMetadata.id()))
                .creationDate(testCaseMetadata.creationDate())
                .title(testCaseMetadata.title())
                .description(testCaseMetadata.description())
                .repositorySource(testCaseMetadata.repositorySource())
                .tags(testCaseMetadata.tags())
                .executions(executions)
                .build()
            )
            .build();
    }
}
