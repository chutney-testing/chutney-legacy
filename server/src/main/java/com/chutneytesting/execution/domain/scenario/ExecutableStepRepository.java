package com.chutneytesting.execution.domain.scenario;

public interface ExecutableStepRepository {

    ExecutableComposedStep findExecutableById(String recordId);

}
