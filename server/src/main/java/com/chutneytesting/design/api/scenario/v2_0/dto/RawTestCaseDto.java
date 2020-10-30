package com.chutneytesting.design.api.scenario.v2_0.dto;

import static java.time.Instant.now;
import static java.util.Collections.emptyList;

import com.chutneytesting.design.api.compose.dto.KeyValue;
import com.chutneytesting.security.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableRawTestCaseDto.class)
@JsonDeserialize(as = ImmutableRawTestCaseDto.class)
@Value.Style(jdkOnly = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface RawTestCaseDto {

    String content();

    Optional<String> id();

    String title();

    Optional<String> description();

    List<String> tags();

    @Value.Default()
    default Instant creationDate() {
        return now();
    }

    @Value.Default()
    default String author() {
        return User.ANONYMOUS_USER.getId();
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
    default List<KeyValue> computedParameters() {
        return emptyList();
    }

}
