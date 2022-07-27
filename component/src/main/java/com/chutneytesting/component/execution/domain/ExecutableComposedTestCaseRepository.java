package com.chutneytesting.component.execution.domain;

public interface ExecutableComposedTestCaseRepository {

    ExecutableComposedTestCase findExecutableById(String scenarioId);

}
