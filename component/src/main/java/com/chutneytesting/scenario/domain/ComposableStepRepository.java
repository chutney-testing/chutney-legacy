package com.chutneytesting.scenario.domain;

import com.chutneytesting.tools.PaginatedDto;
import com.chutneytesting.tools.PaginationRequestParametersDto;
import com.chutneytesting.tools.SortRequestParametersDto;
import java.util.List;

public interface ComposableStepRepository {

    String save(final ComposableStep step);

    ComposableStep findById(final String recordId);

    List<ParentStepId> findParents(String stepId);

    List<ComposableStep> findAll();

    void deleteById(String stepId);
}
