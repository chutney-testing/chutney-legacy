package com.chutneytesting.execution.domain.scenario;

public interface ComposableExecutableTestCaseRepository {

    ComposableExecutableTestCase findExecutableById(String scenarioId);

}
