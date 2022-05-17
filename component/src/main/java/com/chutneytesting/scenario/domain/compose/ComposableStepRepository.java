package com.chutneytesting.scenario.domain.compose;

import com.chutneytesting.tools.PaginatedDto;
import com.chutneytesting.tools.PaginationRequestParametersDto;
import com.chutneytesting.tools.SortRequestParametersDto;
import java.util.List;

public interface ComposableStepRepository {

    String save(final ComposableStep step);

    ComposableStep findById(final String recordId);

    PaginatedDto<ComposableStep> find(PaginationRequestParametersDto paginationParameters, SortRequestParametersDto sortParameters, ComposableStep filters);

    List<ParentStepId> findParents(String stepId);

    List<ComposableStep> findAll();

    void deleteById(String stepId);
}
