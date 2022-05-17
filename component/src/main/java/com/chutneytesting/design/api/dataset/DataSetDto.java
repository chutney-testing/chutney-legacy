package com.chutneytesting.design.api.dataset;

import static java.time.Instant.now;
import static java.util.Collections.emptyList;

import com.chutneytesting.tools.ui.KeyValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDataSetDto.class)
@JsonDeserialize(as = ImmutableDataSetDto.class)
@Value.Style(jdkOnly = true)
public interface DataSetDto {

    Comparator<DataSetDto> dataSetComparator = Comparator
        .comparing(DataSetDto::name, String.CASE_INSENSITIVE_ORDER);

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
}
