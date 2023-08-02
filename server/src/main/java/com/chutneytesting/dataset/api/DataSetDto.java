package com.chutneytesting.dataset.api;

import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import com.chutneytesting.server.core.domain.tools.ui.KeyValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDataSetDto.class)
@JsonDeserialize(as = ImmutableDataSetDto.class)
@Value.Style(jdkOnly = true)
public interface DataSetDto {

    Optional<String> id();
    String name();

    @Value.Default()
    default Integer version() {
        return 0;
    }

    @Value.Default()
    default String description() {
        return "";
    }

    @Value.Default()
    default Instant lastUpdated() {
        return now();
    }

    @Value.Default()
    default List<String> tags() {
        return emptyList();
    }

    @Value.Default()
    @JsonProperty("uniqueValues")
    default List<KeyValue> constants() {
        return emptyList();
    }

    @Value.Default()
    @JsonProperty("multipleValues")
    default List<List<KeyValue>> datatable() {
        return emptyList();
    }

    default boolean hasDuplicatedHeader() {
        if(!datatable().isEmpty()) {
            Set<String> uniqueElements = new HashSet<>();
            List<String> headers = datatable().get(0).stream().map(KeyValue::key).toList();
            return headers.stream().anyMatch(h -> !uniqueElements.add(h));
        }

        return false;
    }
}
