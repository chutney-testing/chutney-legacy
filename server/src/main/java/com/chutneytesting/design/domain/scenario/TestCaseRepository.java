package com.chutneytesting.design.domain.scenario;

import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import java.util.List;

public interface TestCaseRepository {

    String DEFAULT_REPOSITORY_SOURCE = "local";

    String save(GwtTestCase testCase);

    TestCase findById(String scenarioId);

    TestCaseMetadata findMetadataById(String scenarioId);

    List<TestCaseMetadata> findAll();

    void removeById(String scenarioId);

    Integer lastVersion(String testCaseId);

}
