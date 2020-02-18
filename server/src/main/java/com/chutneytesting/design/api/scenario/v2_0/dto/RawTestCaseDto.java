package com.chutneytesting.design.api.scenario.v2_0.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.chutneytesting.design.api.compose.dto.KeyValue;
import java.time.Instant;
import java.util.Collections;
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
        return Instant.now();
    }

    @Value.Default()
    default List<KeyValue> dataSet() { return Collections.emptyList(); }

}
