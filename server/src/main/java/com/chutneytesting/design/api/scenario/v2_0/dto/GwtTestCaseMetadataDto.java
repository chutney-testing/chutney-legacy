package com.chutneytesting.design.api.scenario.v2_0.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.chutneytesting.execution.api.ExecutionSummaryDto;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import graphql.annotations.annotationTypes.GraphQLTypeResolver;
import graphql.kickstart.annotations.GraphQLInterfaceTypeResolver;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableGwtTestCaseMetadataDto.class)
@JsonDeserialize(as = ImmutableGwtTestCaseMetadataDto.class)
@GraphQLTypeResolver(GraphQLInterfaceTypeResolver.class)
public interface GwtTestCaseMetadataDto {

    @GraphQLField
    Optional<String> id();

    @GraphQLField
    String title();

    @GraphQLField
    Optional<String> description();

    @GraphQLField
    Optional<String> repositorySource();

    @GraphQLField
    List<String> tags();

    List<ExecutionSummaryDto> executions();

    @Value.Default()
    default Instant creationDate() {
        return Instant.now();
    }

}
