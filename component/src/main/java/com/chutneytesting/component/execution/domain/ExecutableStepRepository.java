package com.chutneytesting.component.execution.domain;

public interface ExecutableStepRepository {

    ExecutableComposedStep findExecutableById(String recordId);

}
