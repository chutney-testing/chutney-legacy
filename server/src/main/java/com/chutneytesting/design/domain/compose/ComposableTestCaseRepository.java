package com.chutneytesting.design.domain.compose;

import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import java.util.List;

public interface ComposableTestCaseRepository {

    String COMPOSABLE_TESTCASE_REPOSITORY_SOURCE = "ComposableTestCase";

    String save(ComposableTestCase composableTestCase);

    ComposableTestCase findById(String composableTestCaseId);

    List<TestCaseMetadata> findAll();

    void removeById(String testCaseId);

    Integer lastVersion(String composableTestCaseId);
}
