package com.chutneytesting.design.domain.scenario.compose;

import com.chutneytesting.tools.PaginatedDto;
import com.chutneytesting.tools.PaginationRequestParametersDto;
import com.chutneytesting.tools.SortRequestParametersDto;
import java.util.List;

public interface StepRepository {

    String save(final FunctionalStep step);

    FunctionalStep findById(final String recordId);

    List<FunctionalStep> queryByName(String searchQuery);

    PaginatedDto<FunctionalStep> find(PaginationRequestParametersDto paginationParameters, SortRequestParametersDto sortParameters, FunctionalStep filters);

    List<ParentStepId> findParents(String stepId);

    List<FunctionalStep> findAll();

    void deleteById(String stepId);
}
