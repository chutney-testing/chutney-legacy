package com.chutneytesting.design.api.scenario.v2_0.dto;

import static java.time.Instant.now;
import static java.util.Collections.emptyList;

import com.chutneytesting.security.domain.User;
import com.chutneytesting.tools.ui.KeyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLTypeResolver;
import graphql.kickstart.annotations.GraphQLInterfaceTypeResolver;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableRawTestCaseDto.class)
@JsonDeserialize(as = ImmutableRawTestCaseDto.class)
@Value.Style(jdkOnly = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@GraphQLTypeResolver(GraphQLInterfaceTypeResolver.class)
public interface RawTestCaseDto {

    @GraphQLField
    @JsonProperty("content")
    String scenario();

    @GraphQLField
    Optional<String> id();

    @GraphQLField
    String title();

    @GraphQLField
    Optional<String> description();

    @GraphQLField
    List<String> tags();

    @Value.Default()
    default Instant creationDate() {
        return now();
    }

    @Value.Default()
    default String author() {
        return User.ANONYMOUS.id;
    }

    @Value.Default()
    default Instant updateDate() {
        return now();
    }

    @Value.Default()
    default Integer version() {
        return 1;
    }

    @Value.Default()
    @JsonProperty("computedParameters")
    default List<KeyValue> parameters() {
        return emptyList();
    }

}
