package com.chutneytesting.scenario.api.raw.dto;

import com.chutneytesting.execution.api.ExecutionSummaryDto;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableGwtTestCaseMetadataDto.class)
@JsonDeserialize(as = ImmutableGwtTestCaseMetadataDto.class)
public interface GwtTestCaseMetadataDto {

    Optional<String> id();

    String title();

    Optional<String> description();

    Optional<String> repositorySource();

    List<String> tags();

    List<ExecutionSummaryDto> executions();

    @Value.Default()
    default Instant creationDate() {
        return Instant.now();
    }

}
