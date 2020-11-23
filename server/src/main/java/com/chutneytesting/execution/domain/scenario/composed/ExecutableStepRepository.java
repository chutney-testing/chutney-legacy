package com.chutneytesting.execution.domain.scenario.composed;

public interface ExecutableStepRepository {

    ExecutableComposedStep findExecutableById(String recordId);

}
