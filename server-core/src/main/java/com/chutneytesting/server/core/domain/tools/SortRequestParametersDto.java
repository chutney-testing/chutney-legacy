package com.chutneytesting.server.core.domain.tools;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSortRequestParametersDto.class)
@JsonDeserialize(as = ImmutableSortRequestParametersDto.class)
@Value.Style(jdkOnly = true)
public interface SortRequestParametersDto {

    @Nullable
    String sort();

    @Nullable
    String desc();

    @Value.Derived
    default List<String> sortParameters() {
        return sort() != null ? Arrays.asList(sort().split(",")) : Collections.emptyList();
    }

    @Value.Derived
    default List<String> descParameters() {
        return desc() != null ? (desc().length() > 0 ? Arrays.asList(desc().split(",")) : sortParameters()) : Collections.emptyList();
    }
}
