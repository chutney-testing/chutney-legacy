package com.chutneytesting.server.core.domain.tools;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePaginationRequestWrapperDto.class)
@JsonDeserialize(as = ImmutablePaginationRequestWrapperDto.class)
@Value.Style(jdkOnly = true)
public interface PaginationRequestWrapperDto<W> {

    Integer pageNumber();
    Integer elementPerPage();

    @Value.Default
    default Optional<W> wrappedRequest() { return Optional.empty(); }
}
