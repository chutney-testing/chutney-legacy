package com.chutneytesting.component.scenario.domain;

import java.util.List;

public interface ComposableStepRepository {

    String save(final ComposableStep step);

    ComposableStep findById(final String recordId);

    List<ParentStepId> findParents(String stepId);

    List<ComposableStep> findAll();

    void deleteById(String stepId);
}
