package com.chutneytesting.execution.domain;

public interface ExecutableComposedTestCaseRepository {

    ExecutableComposedTestCase findExecutableById(String scenarioId);

}
