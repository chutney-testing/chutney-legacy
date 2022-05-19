package com.chutneytesting.execution.domain;

public interface ExecutableStepRepository {

    ExecutableComposedStep findExecutableById(String recordId);

}
