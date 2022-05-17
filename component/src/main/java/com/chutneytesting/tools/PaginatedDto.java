package com.chutneytesting.tools;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Collections;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePaginatedDto.class)
@JsonDeserialize(as = ImmutablePaginatedDto.class)
@Value.Style(jdkOnly = true)
public interface PaginatedDto<PAGINATED_OBJECT> {

    long totalCount();
    @Value.Default
    default List<PAGINATED_OBJECT> data() { return Collections.emptyList(); }
}
