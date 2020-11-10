package com.chutneytesting.execution.domain.scenario;

public interface ExecutableStepRepository {

    ExecutableComposedFunctionalStep findExecutableById(String recordId);

}
