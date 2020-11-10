package com.chutneytesting.execution.domain.scenario;

public interface ExecutableComposedTestCaseRepository {

    ExecutableComposedTestCase findExecutableById(String scenarioId);

}
