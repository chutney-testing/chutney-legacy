package com.chutneytesting.scenario.domain.compose;

import com.chutneytesting.scenario.domain.TestCaseMetadata;
import java.util.List;

public interface ComposableTestCaseRepository {

    String COMPOSABLE_TESTCASE_REPOSITORY_SOURCE = "ComposableTestCase";

    String save(ComposableTestCase composableTestCase);

    ComposableTestCase findById(String composableTestCaseId);

    List<TestCaseMetadata> findAll();

    void removeById(String testCaseId);

    Integer lastVersion(String composableTestCaseId);

    List<TestCaseMetadata> search(String textFilter);
}
